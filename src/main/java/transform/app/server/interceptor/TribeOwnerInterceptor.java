package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Tribe;
import transform.app.server.model.User;

import static transform.app.server.model.Tribe.TRIBE_ID;

/**
 * 部落拥有者拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class TribeOwnerInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        User user = controller.getAttr("user");
        String tribe_id = controller.getPara(TRIBE_ID);
        if (StringUtils.isEmpty(tribe_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "tribe id can not be null"));
            return;
        }
        Tribe tribe = Tribe.dao.findById(tribe_id);
        if (tribe == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "tribe is not found"));// 找不到部落
        } else if (!user.get(User.USER_ID).equals(tribe.get(Tribe.USER_ID))) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "tribe is not yours"));// 部落不是你的~~
        } else {
            // 继续下去
            controller.setAttr("tribe", tribe);
            inv.invoke();
        }
    }
}
