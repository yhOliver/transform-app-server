package transform.app.server.model;


import com.jfinal.plugin.activerecord.Model;

import java.util.Map;

/**
 * 收货地址表
 */
public class ConsigneeAddress extends Model<ConsigneeAddress> {
    public static String CONSIGNEE_ID = "consignee_id";
    public static String USER_ID = "user_id";
    public static String ADDRESS = "address";
    public static String ZIPCODE = "zipcode";
    public static String CONSIGNEE_TEL = "consignee_tel";
    public static String CONSIGNEE_NAME = "consignee_name";
    public static String CREATETIME = "createtime";
    public static String UPDATETIME = "updatetime";
    public static String STATUS = "status";

    public static final ConsigneeAddress dao = new ConsigneeAddress();

    @Override
    public Map<String, Object> getAttrs() {
        return super.getAttrs();
    }
}
