package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 收藏表
 */
public class Favorite extends Model<Favorite> {

    public static String COLLECTION_ID = "collection_id";
    /**
     * 收藏者id
     */
    public static String USER_ID = "user_id";
    /**
     * 收藏类型 1 帖子 2 文章 3 场馆
     */
    public static String COLLECTION_TYPE = "collection_type";
    /**
     * 收藏实际id
     */
    public static String FK_ID = "fk_id";
    /**
     * 收藏时间
     */
    public static String CREATETIME = "createtime";

    public static Favorite dao = new Favorite();

}
