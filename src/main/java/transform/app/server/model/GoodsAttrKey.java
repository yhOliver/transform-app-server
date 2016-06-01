package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 商品键表
 */
public class GoodsAttrKey extends Model<GoodsAttrKey> {
    /**
     * 属性ID
     */
    public static String ATTR_KEY_ID ="attr_key_id";
    /**
     * 属性名
     */
    public static String ATTR_KEY_NAME ="attr_key_name";

    public static String ATTR_KEY_REQUIRED ="attr_key_required";
    /**
     *类别ID
     */
    public static String CATA_ID ="cata_id";
    /**
     * 商品ID
     */
    public static String GOODS_ID ="goods_id";

    public static GoodsAttrKey dao = new GoodsAttrKey();

}
