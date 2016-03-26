package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子赞表
 */
public class Zan extends Model<Zan> {
    /**
     * 帖子ID
     */
    public static String POST_ID = "post_id";
    /**
     * 赞的用户ID
     */
    public static String USER_ID = "user_id";
}
