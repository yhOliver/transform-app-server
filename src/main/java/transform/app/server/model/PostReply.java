package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子回复表
 */
public class PostReply extends Model<PostReply> {
    /**
     * 回复ID
     */
    public static String REPLY_ID = "reply_id";
    /**
     * 帖子ID
     */
    public static String POST_ID = "post_id";
    /**
     * 回复者ID
     */
    public static String USER_ID = "user_id";
    /**
     * 被回复者ID (0 表示回复帖子)
     */
    public static String REPLY_TO_USER_ID = "reply_to_user_id";
    /**
     * 回复内容
     */
    public static String REPLY_CONTENT = "reply_content";
    /**
     * 回复时间
     */
    public static String REPLY_DATE = "reply_date";

}
