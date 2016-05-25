package transform.app.server.model;


import com.jfinal.plugin.activerecord.Model;

/**
 * 收货地址表
 */
public class ConsigneeAddress extends Model<ConsigneeAddress> {
    public static String CONSIGNEE_ID = "consignee_id";
    public static String USER_ID = "user_id";
    public static String ADDRESS = "address";


    //TODO 增加更多属性


    public static final ConsigneeAddress dao = new ConsigneeAddress();
}
