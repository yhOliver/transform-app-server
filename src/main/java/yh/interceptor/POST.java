package yh.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;

public class POST implements Interceptor {
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        if ("POST".equalsIgnoreCase(controller.getRequest().getMethod()))
            inv.invoke();
        else
            controller.renderJson(new BaseResponse(Code.FAILURE, "请求必须为POST"));
    }
}
