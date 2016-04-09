package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.User;

import static transform.app.server.model.User.USER_ID;

/**
 * 查看用户信息拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class UserStatusInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String user_id = controller.getPara(USER_ID);
        if (StringUtils.isEmpty(user_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "user id can not be null"));
            return;
        }
        User user = User.dao.findFirst("SELECT * FROM tbuser WHERE user_id=? AND status=1", user_id);
        if (user == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "user is not found or is forbidden"));
            return;
        }
        controller.setAttr("user", user);
        inv.invoke();
    }
}
