package yh.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Watch
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-15
 */
public class Watch extends Model<Watch>{

    public static String ID = "id";
    public static String NAME = "name";
    public static String LOCATION = "location";
    public static String TYPE = "type";
    public static String COUNT = "count";
    public static String TIME = "time";

    public static Watch dao = new Watch();

}
