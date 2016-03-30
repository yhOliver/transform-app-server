package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 用户-关注表
 */
public class UserConcern extends Model<UserConcern> {
    /**
     * ID
     */
    public static String ID = "id";
    /**
     * 关注者ID
     */
    public static String CONCERN_ID = "concern_id";
    /**
     * 被关注者ID
     */
    public static String CONCERNED_ID = "concerned_id";
}
