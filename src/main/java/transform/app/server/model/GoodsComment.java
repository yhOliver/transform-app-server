package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 商品评价表
 */
public class GoodsComment extends Model<GoodsComment> {
    public static String GOCO_ID = "goco_id";//主键
    public static String GOODS_ID = "goods_id";//商品ID
    public static String ORDER_ID = "order_id";//订单ID
    public static String USER_ID = "user_id";//用户ID
    public static String COMMENT = "comment";//评论内容
    public static String CREATETIME = "createtime";//评价时间
    public static String STARLEVEL = "starlevel";//好中差评 1,0,-1
    public static String COMMENT_REPLY = "comment_reply";//商家回复
    public static String MEDIAURL = "mediaurl";// 晒图，最多5张，以|分隔
    public static String GOODS_SPEC = "goods_spec";// 商品规格

    public static final GoodsComment dao = new GoodsComment();
}
