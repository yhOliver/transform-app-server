package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.*;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.SMSUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.config.AppProperty;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.interceptor.UserStatusInterceptor;
import transform.app.server.model.RegisterCode;
import transform.app.server.model.User;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static transform.app.server.model.RegisterCode.CODE;
import static transform.app.server.model.User.*;


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
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class AccountAPIController extends BaseAPIController {

    /**
     * 检查用户账号是否被注册*
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
     */
    @Clear
    @Before({POST.class, Tx.class})
    public void sendCode() {
        String user_mobile = getPara(USER_MOBILE);
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
        if (Db.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile) != null) {
            renderJson(new BaseResponse(Code.FAILURE, "mobile already registered"));
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
        renderJson(new BaseResponse("sms sended"));
    }

    /**
     * 用户注册
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
                .save();

        //删除验证码记录
        Db.update("DELETE FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code);

        //返回数据
        renderJson(new BaseResponse("success"));
    }

    /**
     * 登录接口
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
        String sql = "SELECT * FROM tbuser WHERE user_mobile=? AND pwd=?";
        User nowUser = User.dao.findFirst(sql, user_mobile, password);
        if (nowUser == null) {
            renderJson(new BaseResponse(Code.FAILURE, "userName or password is error"));
            return;
        }
        LoginVO vo = new LoginVO();
        Map<String, Object> userInfo = new HashMap<>(nowUser.getAttrs());
        userInfo.remove(PWD);
        vo.setInfo(userInfo);
        vo.setToken(TokenManager.getMe().generateToken(nowUser));
        vo.setConstant(Constant.me());
        renderJson(new BaseResponse("login success", vo));
    }

    /**
     * 查询用户资料(也可查询他人资料)， 不需要登陆
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void view() {
        User user = getAttr("user");
        BaseResponse response = new BaseResponse();
        HashMap<String, Object> map = new HashMap<>(user.getAttrs());
        map.remove(PWD);
        response.setResult(map);
        renderJson(response);
    }

    /**
     * 修改用户资料
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
            Timestamp birth = DateUtils.getBirthday(user_address);
            user.set(USER_BIRTHDAY, birth);
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
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void getAvatar() {
        User user = getAttr("user");
        String user_photo = user.getStr(USER_PHOTO);
        if (StringUtils.isNotEmpty(user_photo)) {
            renderJson(new BaseResponse(user_photo));
        } else {
            renderFailed("user photo is not found");
        }
    }


    /**
     * 获取用户粉丝列表
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void fans() {
        /**
         concern_id 关注 concerned_id
         查找concerned_id的粉丝列表，被哪些用户所关注
         SELECT tu.user_id, tu.user_nickname, tu.user_photo FROM (SELECT * FROM tbuser_concern WHERE concerned_id = ?) tc LEFT JOIN tbuser tu ON tc.concern_id = tu.user_id
         */
        String user_id = getPara(USER_ID);
        List<Record> fs = Db.find("SELECT tu.user_id, tu.user_nickname, tu.user_photo FROM (SELECT * FROM tbuser_concern WHERE concerned_id = ?) tc LEFT JOIN tbuser tu ON tc.concern_id = tu.user_id", user_id);
        renderJson(new BaseResponse(fs));
    }

    /**
     * 获取用户关注列表
     */
    @Clear
    @Before({POST.class, UserStatusInterceptor.class})
    public void concerns() {
        /**
         concern_id 关注 concerned_id
         查找concern_id的关注列表，关注了哪些用户
         SELECT tu.user_id, tu.user_nickname, tu.user_photo FROM (SELECT * FROM tbuser_concern WHERE concern_id = ?) tc LEFT JOIN tbuser tu ON tc.concerned_id = tu.user_id
         */
        String user_id = getPara(USER_ID);
        List<Record> cons = Db.find("SELECT tu.user_id, tu.user_nickname, tu.user_photo FROM (SELECT * FROM tbuser_concern WHERE concern_id = ?) tc LEFT JOIN tbuser tu ON tc.concerned_id = tu.user_id", user_id);
        renderJson(new BaseResponse(cons));
    }
}

