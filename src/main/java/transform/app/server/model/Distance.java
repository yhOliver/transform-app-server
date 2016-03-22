package transform.app.server.model;


import com.jfinal.plugin.activerecord.Model;

/**
 * 设备-场馆距离表
 */
public class Distance extends Model<Distance> {
    /**
     * 设备uuid
     */
    public static String DEVICE_UUID = "device_uuid";

    /**
     * 场馆id
     */
    public static String VENU_ID = "venu_id";

    /**
     * 设备与场馆的距离
     */
    public static String DV_DISTANCE = "dv_distance";

    /**
     * 更新时间
     */
    public static String UPDATETIME = "updatetime";

    public static final Distance dao = new Distance();
}
