package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子-多媒体表（帖子中包含的图片、视频）
 */
public class CardMedia extends Model<CardMedia> {
    /**
     * ID
     */
    public static String MEDIA_ID = "media_id";

    /**
     * 媒体类型
     */
    public static String MEDIA_TYPE = "media_type";

    /**
     * 媒体（图片或视频）地址
     */
    public static String MEDIA_URL = "media_url";

    /**
     * 帖子ID
     */
    public static String CARD_ID = "card_id";


    public static final CardMedia dao = new CardMedia();
}
