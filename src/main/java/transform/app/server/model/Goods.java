package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

import java.util.Map;

/**
 * 商品表
 */
public class Goods extends Model<Goods> {
    public static String GOODS_ID = "goods_id";
    public static String GOODS_NAME = "goods_name";
    public static String CATA_ID = "cata_id";
    public static String CATA_PARENT_ID = "cata_parent_id";
    public static String GOODS_NUMBER = "goods_number";
    public static String GOODS_PRICE = "goods_price";
    public static String GOODS_BRAND = "goods_brand";
    public static String GOODS_DECRIBE_FILE = "goods_decribe_file";
    public static String GOODS_GALLERY = "goods_gallery";
    public static String CREATE_TIME = "create_time";
    public static String GOODS_ISONSALE ="goods_isonsale";

    public static final Goods dao = new Goods();

    @Override
    public Map<String, Object> getAttrs() {
        return super.getAttrs();
    }

}
