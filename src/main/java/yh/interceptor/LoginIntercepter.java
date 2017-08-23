package yh.interceptor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.config.Context;

/**
 * LoginIntercepter
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-09
 */
public class LoginIntercepter implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginIntercepter.class);

    @Override
    public void intercept(Invocation inv) {
        inv.getActionKey();
        Object obj = Context.me().getRequest().getSession().getAttribute("username");
        logger.info(""+ (obj == null));
        if (obj == null){
            inv.getController().renderJson(new BaseResponse(Code.SUCCESS, "未登录"));
            return;
        }else {
            inv.invoke();
        }
    }
}
