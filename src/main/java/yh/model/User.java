package yh.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * @author zhuqi259
 *         2016/3/19
 */
public class User extends Model<User> {
    public static String USER_ID = "user_id";
    public static String USER_NICKNAME = "user_nickname";
    public static String USER_MOBILE = "user_mobile";
    public static String USER_CODE = "user_code";
    public static String USER_PASSWORD = "user_password";
    public static String CREATETIME = "create_time";
    public static String AUTHTIME = "auth_time";
    public static String STATUS = "status";
    public static String type = "type";

    private static final long serialVersionUID = 1L;
    public static final User dao = new User();


    public Integer userId() {
        return getInt(USER_ID);
    }

}
