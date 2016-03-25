package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * @author zhuqi259
 *         2016/3/24
 *         部落成员表
 */
public class TribeMember extends Model<TribeMember> {
    /**
     * ID
     */
    public static String ID = "id";
    /**
     * 部落ID
     */
    public static String TRIBE_ID = "tribe_id";
    /**
     * 部落成员ID(除了创建者)
     */
    public static String USER_ID = "user_id";

    public static final TribeMember dao = new TribeMember();
}
