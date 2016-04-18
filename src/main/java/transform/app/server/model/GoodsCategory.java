package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 商品类别（大小类别，两级）
 */
public class GoodsCategory extends Model<GoodsCategory> {
    /**
     * 类别ID
     */
    public static String CATA_ID = "cata_id";
    /**
     * 父ID
     */
    public static String FATHER_ID = "father_id";
    /**
     * 类别名称
     */
    public static String CATA_NAME = "cata_name";
    /**
     * 类别图片
     */
    public static String CATA_IMG = "cata_img";


    public static GoodsCategory dao = new GoodsCategory();
}
