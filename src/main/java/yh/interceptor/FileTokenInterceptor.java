package yh.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.common.token.TokenManager;
import yh.common.utils.StringUtils;
import yh.model.User;

/**
 * File Token拦截器 需要先取文件，才能getPara
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class FileTokenInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        controller.getFile();
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
