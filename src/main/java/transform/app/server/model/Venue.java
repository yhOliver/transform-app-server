package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

public class Venue extends Model<Venue> {
    public static String VENU_ID = "venu_id";
    public static String VENU_NAME = "venu_name";
    public static String VENU_CITY = "venu_city";
    public static String VENU_ADDRESS = "venu_address";
    public static String VENU_INFO = "venu_info";
    public static String VENU_FACILITY_INFO = "venu_facility_info";
    public static String VENU_STARLEVEL = "venu_starlevel";
    public static String VENU_TEL = "venu_tel";
    public static String VENU_LATITUDE = "venu_latitude";
    public static String VENU_LONGITUDE = "venu_longitude";
    public static String VENU_STARTTIME = "venu_starttime";
    public static String VENU_ENDTIME = "venu_endtime";
    public static String VENU_ISONLINE = "venu_isonline";
    public static String HASWIFI = "haswifi";
    public static String HASBATH = "hasbath";
    public static String IMG0 = "img0";
    public static String IMG1 = "img1";
    public static String IMG2 = "img2";
    public static String IMG3 = "img3";
    public static String IMG4 = "img4";
    public static String IMG5 = "img5";
    public static String CREATETIME = "createtime";
    public static String UPDATETIME = "updatetime";

    public static final Venue dao = new Venue();
}
