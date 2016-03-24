package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * @author zhuqi259
 *         2016/3/24
 *         部落表
 */
public class Tribe extends Model<Tribe> {
    /**
     * 部落ID
     */
    public static String TRIBE_ID = "tribe_id";
    /**
     * 部落名称
     */
    public static String TRIBE_NAME = "tribe_name";
    /**
     * 部落头像
     */
    public static String TRIBE_IMG = "tribe_img";
    /**
     * 部落简介
     */
    public static String TRIBE_INFO = "tribe_info";
    /**
     * 部落创建者ID
     */
    public static String USER_ID = "user_id";
    /**
     * 部落等级
     */
    public static String TRIBE_LEVEL = "tribe_level";
    /**
     * 部落人数
     */
    public static String NUM_OF_MEMBERS = "num_of_members";
    /**
     * 创建时间
     */
    public static String CREATETIME = "createtime";
    /**
     * 更新时间
     */
    public static String UPDATETIME = "updatetime";

    public static final Tribe dao = new Tribe();
}
