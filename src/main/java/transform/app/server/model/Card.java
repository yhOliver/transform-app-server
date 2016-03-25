package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 帖子表
 */
public class Card extends Model<Card> {
    /**
     * 帖子ID
     */
    public static String CARD_ID = "card_id";
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
    public static String CARD_CONTENT = "card_content";
    /**
     * 创建时间
     */
    public static String CARD_DATE = "card_date";
    /**
     * 帖子(软删除)
     */
    public static String CARD_ISEXIST = "card_isexist";
}
