package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 商品属性值表
 */
public class GoodsAttrValue extends Model<GoodsAttrValue> {
    /**
     * 属性值ID
     */
    public static String ATTR_VALUE_ID ="attr_value_id";
    /**
     * 属性值名
     */
    public static String ATTR_VALUE_NAME ="attr_value_name";
    /**
     * 属性ID
     */
    public static String ATTR_KEY_ID ="attr_key_id";
    /**
     * 商品ID
     */
    public static String GOODS_ID ="goods_id";

    public static GoodsAttrValue dao = new GoodsAttrValue();

}
