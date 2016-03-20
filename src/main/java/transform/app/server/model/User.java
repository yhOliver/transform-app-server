package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.Map;

/**
 * @author zhuqi259
 *         2016/3/19
 */
public class User extends Model<User> {
    public static String USER_ID = "user_id";
    public static String USER_NICKNAME = "user_nickname";
    public static String USER_MOBILE = "user_mobile";
    public static String PWD = "pwd";
    public static String USER_ADDRESS = "user_address";
    public static String USER_PHOTO = "user_photo";
    public static String NUM_OF_CARE = "num_of_care";
    public static String NUM_OF_FANS = "num_of_fans";
    public static String NUM_OF_STATUS = "num_of_status";
    public static String USER_BIRTHDAY = "user_birthday";
    public static String USER_SEX = "user_sex";
    public static String USER_HEIGHT = "user_height";
    public static String USER_WEIGHT = "user_weight";
    public static String CREATETIME = "createtime";
    public static String UPDATETIME = "updatetime";
    public static String USER_SIGNATURE = "user_signature";
    public static String STATUS = "status";

    private static final long serialVersionUID = 1L;
    public static final User dao = new User();

    /**
     * 获取用户id*
     *
     * @return 用户id
     */
    public String userId() {
        return getStr(USER_ID);
    }

    /**
     * 检查值是否有效*
     *
     * @param sex 性别值
     * @return 有效性
     */
    public static boolean checkSex(int sex) {
        return sex == 1 || sex == 0;
    }

    @Override
    public Map<String, Object> getAttrs() {
        return super.getAttrs();
    }
}
