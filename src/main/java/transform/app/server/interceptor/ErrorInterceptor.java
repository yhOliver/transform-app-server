package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;

/**
 * 捕获所有api action异常
 *
 * @author zhuqi259
 */
public class ErrorInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(ErrorInterceptor.class);

    @Override
    public void intercept(Invocation inv) {
        try {
            inv.invoke();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            inv.getController().renderJson(new BaseResponse(Code.ERROR, "server error"));
        }
    }
}