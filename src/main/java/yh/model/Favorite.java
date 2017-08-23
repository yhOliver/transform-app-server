package yh.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Favorite
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-19
 */
public class Favorite extends Model<Favorite>{

    public static String ID = "id";
    public static String NAME = "name";
    public static String LOCATION = "location";
    public static String TYPE = "type";
    public static String TIME = "time";
    public static String image = "image";
    public static String description = "description";


    public static final Favorite dao = new Favorite();
}
