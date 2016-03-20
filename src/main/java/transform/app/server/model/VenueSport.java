package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 场馆-运动类别关联表
 */
public class VenueSport extends Model<VenueSport> {
    public static String VESP_ID = "vesp_id"; // 主键
    public static String VENU_ID = "venu_id"; //场馆ID
    public static String SPTY_ID = "spty_id"; // 运动类别ID
    public static String SPTY_TITLE = "spty_title"; // 运动类别描述
    public static String VESP_ISONLINE = "vesp_isonline"; // 是否上线
    public static String VESP_PRICE = "vesp_price"; // 价格

    public static final VenueSport dao = new VenueSport();
}
