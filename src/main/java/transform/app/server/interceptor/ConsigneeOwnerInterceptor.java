package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.ConsigneeAddress;
import transform.app.server.model.User;

import static transform.app.server.model.ConsigneeAddress.CONSIGNEE_ID;

/**
 * 收货地址拥有者拦截器 【只能查看自己的收货地址】
 */
public class ConsigneeOwnerInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        User user = controller.getAttr("user");
        String consignee_id = controller.getPara(CONSIGNEE_ID);
        if (StringUtils.isEmpty(consignee_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "consignee id can not be null"));
            return;
        }
        ConsigneeAddress consigneeAddress = ConsigneeAddress.dao.findById(consignee_id);
        if (consigneeAddress == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "收货地址不存在"));// 收货地址不存在
        } else if (!user.get(User.USER_ID).equals(consigneeAddress.get(ConsigneeAddress.USER_ID))) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "此收货地址不是你的"));// 不是你的~~
        } else {
            // 继续下去
            controller.setAttr("consigneeAddress", consigneeAddress);
            inv.invoke();
        }
    }
}
