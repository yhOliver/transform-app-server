package transform.app.server.model;


import com.jfinal.plugin.activerecord.Model;

/**
 * token表
 */
public class Token extends Model<Token> {
    /**
     * token
     */
    public static String TOKEN = "token";
    public static String USER_ID = "user_id";
    public static String DEADTIME = "deadtime";
}
