package transform.app.server.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 意见反馈实体
 * @author zhuqi259
 */
public class FeedBack extends Model<FeedBack> {

    private static final long serialVersionUID = -1267302372426876814L;
    
    public static final FeedBack dao = new FeedBack();

    /**
     * 唯一id
     */
	public static String ID = "id";

    /**
     * 反馈用户的id*
     */
	public static String USER_ID = "user_id";

    /**
     * 反馈时间*
     */
	public static String CREATETIME = "createtime";

    /**
     * 反馈内容*
     */
	public static String SUGGESTION = "suggestion";
}
