package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.User;

/**
 * Token拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class TokenInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String token = controller.getPara("token");
        if (StringUtils.isEmpty(token)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "token can not be null"));
            return;
        }
        User user = TokenManager.getMe().validate(token);
        if (user == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "token is invalid"));
            return;
        }
        controller.setAttr("user", user);
        inv.invoke();
    }
}
