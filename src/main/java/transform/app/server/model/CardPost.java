package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子回复表
 */
public class CardPost extends Model<CardPost> {
    /**
     * 回复ID
     */
    public static String POST_ID = "post_id";
    /**
     * 帖子ID
     */
    public static String CARD_ID = "card_id";
    /**
     * 回复者ID
     */
    public static String USER_ID = "user_id";
    /**
     * 被回复者ID (0 表示回复帖子)
     */
    public static String REPLY_USER_ID = "reply_user_id";
    /**
     * 回复内容
     */
    public static String POST_CONTENT = "post_content";
    /**
     * 回复时间
     */
    public static String POST_DATE = "post_date";
    /**
     * 回复(软删除)
     */
    public static String POST_ISEXIST = "post_isexist";
}
