package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Venue;

import static transform.app.server.model.Distance.VENU_ID;

/**
 * 场馆拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class VenueStatusInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String venu_id = controller.getPara(VENU_ID);
        if (StringUtils.isEmpty(venu_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "venue id can not be null"));
            return;
        }
        // 已发布的该类别场馆
        Venue venue = Venue.dao.findFirst("SELECT * FROM tbvenue WHERE venu_id=? AND venu_isonline=1", venu_id);
        if (venue == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "venue is not existed or offline"));
        } else {
            // 继续下去
            controller.setAttr("venue", venue);
            inv.invoke();
        }
    }
}
