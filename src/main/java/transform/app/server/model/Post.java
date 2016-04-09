package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子表
 */
public class Post extends Model<Post> {
    /**
     * 帖子ID
     */
    public static String POST_ID = "post_id";
    /**
     * 部落ID
     */
    public static String TRIBE_ID = "tribe_id";
    /**
     * 帖子创建者ID
     */
    public static String USER_ID = "user_id";
    /**
     * 设备名称(来自 iphone 6S)
     */
    public static String DEVICE_NAME = "device_name";
    /**
     * 帖子内容
     */
    public static String POST_CONTENT = "post_content";
    /**
     * 图片或视频列表
     */
    public static String MEDIA_URLS = "media_urls";
    /**
     * 创建时间
     */
    public static String POST_DATE = "post_date";
    /**
     * 评论数
     */
    public static String NUM_OF_REPLY = "num_of_reply";
    /**
     * 赞数
     */
    public static String NUM_OF_ZAN = "num_of_zan";
    /**
     * 软删除状态
     */
    public static String STATUS = "status";

    public static Post dao = new Post();
}
