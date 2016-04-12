package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Tribe;

import static transform.app.server.model.Tribe.TRIBE_ID;

/**
 * 部落拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class TribeStatusInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String tribe_id = controller.getPara(TRIBE_ID);
        if (StringUtils.isEmpty(tribe_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "tribe id can not be null"));
            return;
        }
        Tribe tribe = Tribe.dao.findById(tribe_id);
        if (tribe == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "tribe is not found"));// 找不到部落
        } else {
            // 继续下去
            controller.setAttr("tribe", tribe);
            inv.invoke();
        }
    }
}
