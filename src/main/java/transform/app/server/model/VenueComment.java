package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 场馆评价表
 */
public class VenueComment extends Model<VenueComment> {
    public static String VECO_ID = "veco_id"; // 主键
    public static String VENU_ID = "venu_id"; //场馆ID
    public static String USER_ID = "user_id"; // 用户会员ID
    public static String CONTENT = "content"; // 评价内容
    public static String CREATETINE = "createtime"; // 评价时间

    public static final VenueComment dao = new VenueComment();
}
