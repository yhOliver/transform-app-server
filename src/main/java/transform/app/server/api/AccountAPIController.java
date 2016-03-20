package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import transform.app.server.common.Require;
import transform.app.server.common.bean.*;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.SMSUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.config.AppProperty;
import transform.app.server.interceptor.GET;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.RegisterCode;
import transform.app.server.model.User;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static transform.app.server.model.RegisterCode.CODE;
import static transform.app.server.model.User.*;


/**
 * 用户账号相关的接口*
 * <p>
 * 检查账号是否被注册: GET /api/account/checkUser
 * 发送注册验证码: POST /api/account/sendCode
 * 注册: POST /api/account/register
 * 登录： POST /api/account/login
 * 查询用户资料: GET /api/account/profile
 * 修改用户资料: POST /api/account/profile
 * 修改密码: POST /api/account/password
 * 修改头像: POST /api/account/avatar
 *
 * @author malongbo
 */
@Before(TokenInterceptor.class)
public class AccountAPIController extends BaseAPIController {

    /**
     * 检查用户账号是否被注册*
     */
    @Clear
    @Before(GET.class)
    public void checkUser() {
        String user_mobile = getPara(USER_MOBILE);
        if (StringUtils.isEmpty(user_mobile)) {
            renderArgumentError("user mobile can not be null");
            return;
        }
        //检查手机号码是否被注册
        boolean exists = Db.findFirst("SELECT * FROM tbuser WHERE user_mobile=?", user_mobile) != null;
        renderJson(new BaseResponse(exists ? Code.SUCCESS : Code.FAIL, exists ? "registered" : "unregistered"));
    }

    /**
     * 1. 检查是否被注册*
     * 2. 发送短信验证码*
     */
    @Clear
    @Before(POST.class)
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
            renderJson(new BaseResponse(Code.ACCOUNT_EXISTS, "mobile already registered"));
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
    @Before(POST.class)
    public void register() {
        //必填信息
        String user_mobile = getPara(USER_MOBILE);//手机号
        int code = getParaToInt(CODE, 0);//手机验证码
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
            renderJson(new BaseResponse(Code.ACCOUNT_EXISTS, "mobile already registered"));
            return;
        }

        //检查验证码是否有效
        if (Db.findFirst("SELECT * FROM t_register_code WHERE mobile=? AND code = ?", user_mobile, code) == null) {
            renderJson(new BaseResponse(Code.CODE_ERROR, "code is invalid"));
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
        LoginResponse response = new LoginResponse();
        if (nowUser == null) {
            response.setCode(Code.FAIL).setMessage("userName or password is error");
            renderJson(response);
            return;
        }
        Map<String, Object> userInfo = new HashMap<>(nowUser.getAttrs());
        userInfo.remove(PWD);
        response.setInfo(userInfo);
        response.setMessage("login success");
        response.setToken(TokenManager.getMe().generateToken(nowUser));
        response.setConstant(Constant.me());
        renderJson(response);
    }

    /**
     * 资料相关的接口
     */
    public void profile() {
        String method = getRequest().getMethod();
        if ("GET".equalsIgnoreCase(method)) { //查询资料
            getProfile();
        } else if ("POST".equalsIgnoreCase(method)) { //修改资料
            updateProfile();
        } else {
            render404();
        }
    }


    /**
     * 查询用户资料
     */
    private void getProfile() {
        String userId = getPara(USER_ID);
        User resultUser;
        if (StringUtils.isNotEmpty(userId)) {
            resultUser = User.dao.findById(userId);
        } else {
            resultUser = getUser();
        }
        DatumResponse response = new DatumResponse();
        if (resultUser == null) {
            response.setCode(Code.FAIL).setMessage("user is not found");
        } else {
            HashMap<String, Object> map = new HashMap<>(resultUser.getAttrs());
            map.remove(PWD);
            response.setDatum(map);
        }
        renderJson(response);
    }

    /**
     * 修改用户资料
     */
    private void updateProfile() {
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
            renderJson(response.setCode(update ? Code.SUCCESS : Code.FAIL).setMessage(update ? "update success" : "update failed"));
        } else {
            renderArgumentError("must set profile");
        }
    }

    /**
     * 修改密码
     */
    @Before(POST.class)
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
            boolean flag = nowUser.set(PWD, newPwd).update();
            renderJson(new BaseResponse(flag ? Code.SUCCESS : Code.FAIL, flag ? "success" : "failed"));
        } else {
            renderJson(new BaseResponse(Code.FAIL, "oldPwd is invalid"));
        }
    }

    /**
     * 修改头像接口
     * /api/account/avatar
     */
    @Before(POST.class)
    public void avatar() {
        String avatar = getPara(USER_PHOTO);
        if (!notNull(Require.me()
                .put(avatar, "avatar url can not be null"))) {
            return;
        }
        getUser().set(USER_PHOTO, avatar).update();
        renderSuccess("success");
    }
}

