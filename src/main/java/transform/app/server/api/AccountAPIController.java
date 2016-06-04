package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.bean.Constant;
import transform.app.server.common.bean.LoginVO;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.SMSUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.config.AppProperty;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.interceptor.UserStatusInterceptor;
import transform.app.server.model.Post;
import transform.app.server.model.RegisterCode;
import transform.app.server.model.User;
import transform.app.server.model.UserConcern;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static transform.app.server.model.RegisterCode.CODE;
import static transform.app.server.model.User.*;
import static transform.app.server.model.UserConcern.CONCERNED_ID;
import static transform.app.server.model.UserConcern.CONCERN_ID;


/**
 * 用户账号相关的接口*
 * <p>
 * 检查账号是否被注册: POST /api/account/checkUser
 * 发送注册验证码: POST /api/account/sendCode
 * 注册: POST /api/account/register
 * 登录： POST /api/account/login
 * 查询用户资料: POST /api/account/view
 * 修改用户资料: POST /api/account/update
 * 修改密码: POST /api/account/password
 * 修改头像: POST /api/account/avatar
 * 获取头像: POST /api/account/getAvatar
 * 获取用户粉丝列表: POST /api/account/fans
 * 获取用户关注列表: POST /api/account/concerns
 * 用户动态: POST /api/account/posts
 * 关注用户（取消关注）: POST /api/account/concern
 * <p>
 * 修改手机号: POST /api/account/changeMobile
 * 重置密码（忘记密码）: POST /api/account/resetPwd
 * <p>
 * 我的预约: POST /api/account/reservations
 * 查询用户列表: POST /api/account/search
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class AccountAPIController extends BaseAPIController {
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 检查用户账号是否被注册*
     * <p>
     * 无登陆约束，只需POST
     */
    @Clear
    @Before(POST.class)
    public void checkUser() {
        String user_mobile = getPara(USER_MOBILE);
        if (StringUtils.isEmpty(user_mobile)) {
            renderArgumentError("user mobile can not be null");
            return;
        }
        //检查手机号码是否被注册
        boolean exists = Db.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile) != null;
        renderJson(new BaseResponse(exists ? Code.SUCCESS : Code.FAILURE, exists ? "registered" : "unregistered"));
    }

    /**
     * 1. 检查是否被注册*
     * 2. 发送短信验证码*
     * <p>
     * 无登陆约束，只需POST，事务
     */
    @Clear
    @Before({POST.class, Tx.class})
    public void sendCode() {
        String user_mobile = getPara(USER_MOBILE);
        int p = getParaToInt("p", 0); // 标志 [0：注册， 1：修改手机号，2：重置密码]
        if (StringUtils.isEmpty(user_mobile)) {
            renderArgumentError("user mobile can not be null");
            return;
        }

        //检查手机号码有效性
        if (!SMSUtils.isMobileNo(user_mobile)) {
            renderArgumentError("mobile number is invalid");
            return;
        }

        //检查手机号码是否被注册
        Record someOne = Db.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile);
        if (p == 0 || p == 1) { // 注册 或者 修改手机号， 当前传过来的手机号都应当未被注册
            if (someOne != null) {
                renderFailed("mobile already registered");
                return;
            }
        } else if (p == 2) { // 重置密码时发送验证码
            if (someOne == null) {
                renderFailed("mobile is not registered");
                return;
            }
        } else {
            renderFailed("param p is wrong");
            return;
        }

        String smsCode = SMSUtils.randomSMSCode(6);
        //发送短信验证码
        if (!SMSUtils.sendCode(user_mobile, smsCode)) {
            renderFailed("sms send failed");
            return;
        }

        //保存验证码数据
        RegisterCode registerCode = new RegisterCode()
                .set(RegisterCode.MOBILE, user_mobile)
                .set(CODE, smsCode);

        //保存数据
        if (Db.findFirst("SELECT * FROM t_register_code WHERE mobile=?", user_mobile) == null) {
            registerCode.save();
        } else {
            registerCode.update();
        }
        // renderJson(new BaseResponse("sms sended"));
        renderJson(new BaseResponse().setSuccess(Code.SUCCESS).setMsg("sms sended").setResult(smsCode));
    }

    /**
     * 用户注册
     * <p>
     * 无登陆约束，只需POST，事务
     */
    @Clear
    @Before({POST.class, Tx.class})
    public void register() {
        //必填信息
        String user_mobile = getPara(USER_MOBILE);//手机号
        String code = getPara(CODE);//手机验证码
        String password = getPara(PWD);//密码 (已经加密过了)

        //校验必填项参数
        if (!notNull(Require.me()
                .put(user_mobile, "user mobile can not be null")
                .put(code, "code can not be null")//根据业务需求决定是否使用此字段
                .put(password, "password can not be null"))) {
            return;
        }

        //检查账户是否已被注册
        if (Db.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile) != null) {
            renderJson(new BaseResponse(Code.FAILURE, "mobile already registered"));
            return;
        }

        //检查验证码是否有效
        if (Db.findFirst("SELECT * FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code) == null) {
            renderJson(new BaseResponse(Code.FAILURE, "code is invalid"));
            return;
        }

        //保存用户数据
        String userId = RandomUtils.randomCustomUUID();
        String avatar = AppProperty.me().defaultUserAvatar();
        new User()
                .set(USER_ID, userId)
                .set(USER_MOBILE, user_mobile)
                .set(PWD, password)
                .set(CREATETIME, DateUtils.currentTimeStamp())
                .set(UPDATETIME, DateUtils.currentTimeStamp())
                .set(USER_PHOTO, avatar) // 默认头像
                .set(NUM_OF_CARE, 0)
                .set(NUM_OF_FANS, 0)
                .set(NUM_OF_STATUS, 0)
                .set(STATUS, 1)
                .save();

        //删除验证码记录
        Db.update("DELETE FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code);

        //返回数据
        // renderJson(new BaseResponse("success"));

        String sql = "SELECT * FROM tbuser WHERE user_mobile=? AND pwd=? AND status=1"; // 禁用字段=1
        User nowUser = User.dao.findFirst(sql, user_mobile, password);
        if (nowUser == null) {
            renderJson(new BaseResponse(Code.FAILURE, "userName or password is error, or the user is forbidden"));
            return;
        }
        LoginVO vo = new LoginVO();
        vo.setToken(TokenManager.getMe().generateToken(nowUser));
        renderJson(new BaseResponse(Code.SUCCESS, "register success", vo));
    }


    /**
     * 修改手机号: POST /api/account/changeMobile
     * <p>
     * POST、登陆、事务
     */
    @Before(Tx.class)
    public void changeMobile() {
        String user_mobile = getPara(USER_MOBILE);// 新手机号
        String code = getPara(CODE);// 手机验证码
        String password = getPara(PWD);// 密码 (已经加密过了)，需要验证
        //校验必填项参数
        if (!notNull(Require.me()
                .put(user_mobile, "user new mobile can not be null")
                .put(code, "code can not be null")
                .put(password, "password can not be null"))) {
            return;
        }

        User user = getUser();
        // 手机号不会与原来相同 【sendCode满足】
        // 检查密码是否正确
        if (!password.equals(user.getStr(PWD))) {
            renderFailed("password is not right");
            return;
        }
        //检查验证码是否有效
        if (Db.findFirst("SELECT * FROM t_register_code WHERE mobile=? AND code=?", user_mobile, code) == null) {
            renderFailed("code is invalid");
            return;
        }

        //删除验证码记录
        Db.update("DELETE FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code);

        // 修改当前用户手机号
        user.set(UPDATETIME, DateUtils.currentTimeStamp()); // 更新时间
        user.set(USER_MOBILE, user_mobile);
        boolean update = user.update();
        renderJson(new BaseResponse().setSuccess(update ? Code.SUCCESS : Code.FAILURE)
                .setMsg(update ? "update mobile success" : "update mobile failed"));
    }

    /**
     * 重置密码（忘记密码）: POST /api/account/resetPwd
     * <p>
     * POST、登陆、事务
     */
    @Clear
    @Before({POST.class, Tx.class})
    public void resetPwd() {
        String user_mobile = getPara(USER_MOBILE);// 手机号
        String code = getPara(CODE);// 手机验证码
        String password = getPara(PWD);// 新密码 (已经加密过了)

        //校验必填项参数
        if (!notNull(Require.me()
                .put(user_mobile, "user new mobile can not be null")
                .put(code, "code can not be null")
                .put(password, "password can not be null"))) {
            return;
        }

        // 根据手机号查找当前用户
        User someOne = User.dao.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile);
        if (someOne == null) {
            /**
             * @see transform.app.server.api.AccountAPIController.sendCode
             */
            renderFailed("mobile is not registered"); // 重复检测了一下
            return;
        }
        //检查验证码是否有效
        if (Db.findFirst("SELECT * FROM t_register_code WHERE mobile=? AND code=?", user_mobile, code) == null) {
            renderFailed("code is invalid");
            return;
        }

        //删除验证码记录
        Db.update("DELETE FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code);

        // 重置密码
        someOne.set(UPDATETIME, DateUtils.currentTimeStamp()); // 更新时间
        someOne.set(PWD, password);
        boolean update = someOne.update();
        renderJson(new BaseResponse().setSuccess(update ? Code.SUCCESS : Code.FAILURE)
                .setMsg(update ? "reset password success" : "reset password failed"));
    }

    /**
     * 登录接口
     * <p>
     * 无登陆约束，只需POST
     */
    @Clear
    @Before(POST.class)
    public void login() {
        String user_mobile = getPara(USER_MOBILE);
        String password = getPara(PWD);
        //校验参数, 确保不能为空
        if (!notNull(Require.me()
                .put(user_mobile, "user mobile can not be null")
                .put(password, "password can not be null")
        )) {
            return;
        }
        String sql = "SELECT * FROM tbuser WHERE user_mobile=? AND pwd=? AND status=1";
        User nowUser = User.dao.findFirst(sql, user_mobile, password);
        if (nowUser == null) {
            renderJson(new BaseResponse(Code.FAILURE, "userName or password is error, or the user is forbidden"));
            return;
        }
        LoginVO vo = new LoginVO();
        Map<String, Object> userInfo = new HashMap<>(nowUser.getAttrs());
        userInfo.remove(PWD);
        vo.setInfo(userInfo);
        vo.setToken(TokenManager.getMe().generateToken(nowUser));
        vo.setConstant(Constant.me());
        renderJson(new BaseResponse(Code.SUCCESS, "login success", vo));
    }

    /**
     * 查询用户资料(也可查询他人资料)
     * <p>
     * 无登陆约束，只需POST，检查该用户存在
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void view() {
        //  User user = getAttr("user");
        String user_id = getPara(USER_ID); // 用户ID - 查看此用户的详情
        // 登陆状态 与 非登陆状态
        String token = getPara("token");
        if (StringUtils.isNotEmpty(token)) {
            User user = TokenManager.getMe().validate(token);
            if (user == null) {
                renderFailed("token is invalid");
                return;
            }
            String concern_id = user.userId(); // 当前登录用户，判断其是否关注了上面的用户
            // 登陆状态
            /**
             * SELECT tu.user_id, tu.user_nickname, tu.user_mobile, tu.user_address, tu.user_photo, tu.num_of_care, tu.num_of_fans, tu.num_of_status, tu.user_birthday, tu.user_sex, tu.user_height, tu.user_weight, tu.user_signature, (CASE WHEN tuc.id IS NULL THEN 0 ELSE 1 END) AS concern_status FROM (SELECT * FROM tbuser WHERE user_id = ?) tu LEFT JOIN (SELECT * FROM tbuser_concern WHERE concern_id = ?) tuc ON tu.user_id = tuc.concerned_id
             */
            Record record = Db.findFirst("SELECT tu.user_id, tu.user_nickname, tu.user_mobile, tu.user_address, tu.user_photo, tu.num_of_care, tu.num_of_fans, tu.num_of_status, tu.user_birthday, tu.user_sex, tu.user_height, tu.user_weight, tu.user_signature, (CASE WHEN tuc.id IS NULL THEN 0 ELSE 1 END) AS concern_status FROM (SELECT * FROM tbuser WHERE user_id = ?) tu LEFT JOIN (SELECT * FROM tbuser_concern WHERE concern_id = ?) tuc ON tu.user_id = tuc.concerned_id", user_id, concern_id);
            renderJson(new BaseResponse(Code.SUCCESS, "", record));
        } else {
            // 未登陆
            /**
             * SELECT user_id, user_nickname, user_mobile, user_address, user_photo, num_of_care, num_of_fans, num_of_status, user_birthday, user_sex, user_height, user_weight, user_signature, 0 AS concern_status FROM tbuser WHERE user_id = ?
             */
            Record record = Db.findFirst("SELECT user_id, user_nickname, user_mobile, user_address, user_photo, num_of_care, num_of_fans, num_of_status, user_birthday, user_sex, user_height, user_weight, user_signature, 0 AS concern_status FROM tbuser WHERE user_id = ?", user_id);
            renderJson(new BaseResponse(Code.SUCCESS, "", record));
        }
    }

    /**
     * 修改用户资料
     * （修改自身信息）
     * POST，登陆状态，事务
     */
    @Before(Tx.class)
    public void update() {
        boolean flag = false;
        BaseResponse response = new BaseResponse();
        User user = getUser();
        String nickName = getPara(USER_NICKNAME);
        if (StringUtils.isNotEmpty(nickName)) {
            user.set(USER_NICKNAME, nickName);
            flag = true;
        }

        String avatar = getPara(USER_PHOTO);
        if (StringUtils.isNotEmpty(avatar)) {
            user.set(USER_PHOTO, avatar);
            flag = true;
        }

        //修改性别
        Integer sex = getParaToInt(USER_SEX, null);
        if (null != sex) {
            if (!User.checkSex(sex)) {
                renderArgumentError("sex is invalid");
                return;
            }
            user.set(USER_SEX, sex);
            flag = true;
        }

        //个性签名 USER_SIGNATURE
        String user_signature = getPara(USER_SIGNATURE);
        if (StringUtils.isNotEmpty(user_signature)) {
            user.set(USER_SIGNATURE, user_signature);
            flag = true;
        }
        // USER_ADDRESS
        String user_address = getPara(USER_ADDRESS);
        if (StringUtils.isNotEmpty(user_address)) {
            user.set(USER_ADDRESS, user_address);
            flag = true;
        }
        // USER_BIRTHDAY
        String user_birthday = getPara(USER_BIRTHDAY);
        if (StringUtils.isNotEmpty(user_birthday)) {
            // 生日格式 : yyyy-MM-dd
            // Timestamp birth = DateUtils.getBirthday(user_birthday);
            user.set(USER_BIRTHDAY, user_birthday);
            flag = true;
        }
        // USER_HEIGHT
        String user_height = getPara(USER_HEIGHT);
        if (StringUtils.isNotEmpty(user_height)) {
            user.set(USER_HEIGHT, user_height);
            flag = true;
        }
        // USER_WEIGHT
        String user_weight = getPara(USER_WEIGHT);
        if (StringUtils.isNotEmpty(user_weight)) {
            user.set(USER_WEIGHT, user_weight);
            flag = true;
        }
        user.set(UPDATETIME, DateUtils.currentTimeStamp()); // 更新时间
        if (flag) {
            boolean update = user.update();
            renderJson(response.setSuccess(update ? Code.SUCCESS : Code.FAILURE).setMsg(update ? "update success" : "update failed"));
        } else {
            renderArgumentError("must set profile");
        }
    }

    /**
     * 修改密码
     * <p>
     * POST，登陆状态，事务
     */
    @Before(Tx.class)
    public void password() {
        //根据用户id，查出这个用户的密码，再跟传递的旧密码对比，一样就更新，否则提示旧密码错误
        String oldPwd = getPara("oldPwd");
        String newPwd = getPara("newPwd");
        if (!notNull(Require.me()
                .put(oldPwd, "old password can not be null")
                .put(newPwd, "new password can not be null"))) {
            return;
        }
        //用户真实的密码
        User nowUser = getUser();
        if (oldPwd.equalsIgnoreCase(nowUser.getStr(PWD))) {
            boolean flag = nowUser.set(PWD, newPwd)
                    .set(UPDATETIME, DateUtils.currentTimeStamp()).update();
            renderJson(new BaseResponse(flag ? Code.SUCCESS : Code.FAILURE, flag ? "success" : "failed"));
        } else {
            renderJson(new BaseResponse(Code.FAILURE, "oldPwd is invalid"));
        }
    }

    /**
     * 修改头像接口
     * /api/account/avatar
     * <p>
     * POST，登陆状态，事务
     */
    @Before(Tx.class)
    public void avatar() {
        String avatar = getPara(USER_PHOTO);
        if (!notNull(Require.me()
                .put(avatar, "avatar url can not be null"))) {
            return;
        }
        boolean update = getUser().set(USER_PHOTO, avatar)
                .set(UPDATETIME, DateUtils.currentTimeStamp()).update();
        renderJson(new BaseResponse().setSuccess(update ? Code.SUCCESS : Code.FAILURE).setMsg(update ? "update avatar success" : "update avatar failed"));
    }

    /**
     * 获取头像接口(也可获取他人头像)
     * /api/account/getAvatar
     * <p>
     * 无登陆约束，只需POST，检查该用户存在
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void getAvatar() {
        User user = getAttr("user");
        String user_photo = user.getStr(USER_PHOTO);
        if (StringUtils.isNotEmpty(user_photo)) {
            renderJson(new BaseResponse(Code.SUCCESS, "", user_photo));
        } else {
            renderFailed("user photo is not found");
        }
    }


    /**
     * 获取用户粉丝列表
     * <p>
     * 无登陆约束，只需POST，检查该用户存在
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void fans() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         concern_id 关注 concerned_id
         查找concerned_id的粉丝列表，被哪些用户所关注
         SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature
         FROM (SELECT * FROM tbuser_concern WHERE concerned_id = ?) tc LEFT JOIN tbuser tu ON tc.concern_id = tu.user_id
         */
        String user_id = getPara(USER_ID);
        Page<Record> fs = Db.paginate(pageNumber, pageSize, "SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature",
                "FROM (SELECT * FROM tbuser_concern WHERE concerned_id = ?) tc LEFT JOIN tbuser tu ON tc.concern_id = tu.user_id", user_id); // LEFT JOIN 没问题
        renderJson(new BaseResponse(Code.SUCCESS, "", fs));
    }

    /**
     * 获取用户关注列表
     * <p>
     * 无登陆约束，只需POST，检查该用户存在
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void concerns() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         concern_id 关注 concerned_id
         查找concern_id的关注列表，关注了哪些用户
         SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature
         FROM (SELECT * FROM tbuser_concern WHERE concern_id = ?) tc LEFT JOIN tbuser tu ON tc.concerned_id = tu.user_id
         */
        String user_id = getPara(USER_ID);
        Page<Record> cons = Db.paginate(pageNumber, pageSize, "SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature",
                "FROM (SELECT * FROM tbuser_concern WHERE concern_id = ?) tc LEFT JOIN tbuser tu ON tc.concerned_id = tu.user_id", user_id); // LEFT JOIN 没问题
        renderJson(new BaseResponse(Code.SUCCESS, "", cons));
    }


    /**
     * 用户动态 => 用户的帖子列表
     * <p>
     * 无登陆约束，只需POST，检查该用户存在
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void posts() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String user_id = getPara(Post.USER_ID);
        Page<Record> latestThread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo",
                "FROM (SELECT * FROM tbpost WHERE user_id = ?) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC", user_id); // LEFT JOIN 没问题
        renderJson(new BaseResponse(Code.SUCCESS, "", latestThread));
    }

    /**
     * 关注用户（取消关注）: (需要登录，被关注者必须存在)
     * /api/account/concern
     * <p>
     * POST、登陆状态、事务、被关注者存在
     */
    @Before(Tx.class)
    public void concern() {
        User user = getAttr("user");
        String concerned_id = getPara(CONCERNED_ID); // 被关注者ID
        if (StringUtils.isEmpty(concerned_id)) {
            renderFailed("concerned id can not be null");
            return;
        }
        User concerned_user = User.dao.findById(concerned_id);
        if (concerned_user == null) {
            renderFailed("concerned user is not found");
            return;
        }
        String concern_id = user.userId();
        if (concern_id.equals(concerned_id)) {
            renderFailed("you can not concern yourself");
            return;
        }
        int concern_flag = getParaToInt("concern_flag", 1); // 1关注、 0取消关注
        if (concern_flag == 1) {
            // 查看是否已经关注？
            if (Db.findFirst("SELECT * FROM tbuser_concern WHERE concern_id=? AND concerned_id=?", concern_id, concerned_id) == null) {
                // 增加关注记录
                boolean saved = new UserConcern()
                        .set(UserConcern.ID, RandomUtils.randomCustomUUID())
                        .set(CONCERN_ID, concern_id)
                        .set(CONCERNED_ID, concerned_id)
                        .set(UserConcern.OCCURRENCE_TIME, DateUtils.currentTimeStamp())
                        .save();
                if (saved) {
                    // 用户关注数+1
                    Db.update("UPDATE tbuser SET num_of_care = num_of_care+1 WHERE user_id = ?", concern_id);
                    // 被关注者粉丝数+1
                    Db.update("UPDATE tbuser SET num_of_fans = num_of_fans+1 WHERE user_id = ?", concerned_id);
                    renderSuccess("concern success");
                } else {
                    renderFailed("concern failed");
                }
            } else {
                renderFailed("you have already concerned this one");
            }
        } else {
            // 查看是否已经关注？
            if (Db.findFirst("SELECT * FROM tbuser_concern WHERE concern_id=? AND concerned_id=?", concern_id, concerned_id) != null) {
                // 删除关注记录
                int deleted = Db.update("DELETE FROM tbuser_concern WHERE concern_id=? AND concerned_id=?", concern_id, concerned_id);
                if (deleted > 0) {
                    // 用户关注数-1
                    Db.update("UPDATE tbuser SET num_of_care = num_of_care-1 WHERE user_id = ?", concern_id);
                    // 被关注者粉丝数-1
                    Db.update("UPDATE tbuser SET num_of_fans = num_of_fans-1 WHERE user_id = ?", concerned_id);
                    renderSuccess("unconcern success");
                } else {
                    renderFailed("unconcern failed");
                }
            } else {
                renderFailed("you have not concerned this one, no need to unconcern");
            }
        }
    }

    /**
     * 我的预约: POST /api/account/reservations
     * <p>
     * POST、登陆状态
     */
    public void reservations() {
        User user = getAttr("user");
        String user_id = user.userId();
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         * SELECT *
         * FROM venue_subscribe WHERE user_id = ? ORDER BY createtime DESC
         */
        Page<Record> result = Db.paginate(pageNumber, pageSize, "SELECT *",
                "FROM venue_subscribe WHERE user_id = ? ORDER BY createtime DESC", user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", result));
    }

    /**
     * 查询用户列表[模糊查询] TODO 待确定模糊查询用户列表需求
     * <p>
     * POST
     */
    @Clear
    @Before(POST.class)
    public void search() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String wd = getPara("wd");
        if (!notNull(Require.me().put(wd, "search word can not be null"))) {
            return;
        }
        wd = "%" + wd + "%";
        Page<Record> users = Db.paginate(pageNumber, pageSize, "SELECT user_id, user_nickname, user_mobile, user_photo", "FROM tbuser WHERE user_nickname LIKE ? OR user_mobile LIKE ?", wd, wd);
        renderJson(new BaseResponse(Code.SUCCESS, "", users));
    }
}

