package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 运动分类，运动类别
 * 羽毛球、瑜伽、游泳等等
 */
public class SportType extends Model<SportType> {
    public static String SPTY_ID = "spty_id";
    public static String SPTY_NAME = "spty_name";
    public static String SPTY_IMG = "spty_img";
    public static String SPTY_ICON = "spty_icon";
    public static String SPTY_SEQ = "spty_seq";

    public static final SportType dao = new SportType();
}
