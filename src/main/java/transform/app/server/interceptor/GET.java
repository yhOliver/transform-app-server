package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;

public class GET implements Interceptor {
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        if ("GET".equalsIgnoreCase(controller.getRequest().getMethod()))
            inv.invoke();
        else
            controller.renderJson(new BaseResponse(Code.NOT_FOUND));
    }
}
