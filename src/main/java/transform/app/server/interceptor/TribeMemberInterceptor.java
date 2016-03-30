package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Tribe;
import transform.app.server.model.User;

import static transform.app.server.model.Tribe.TRIBE_ID;

/**
 * 部落成员拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class TribeMemberInterceptor implements Interceptor {

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
        } else {
            String user_id = user.get(User.USER_ID);
            if (!user_id.equals(tribe.get(Tribe.USER_ID)) && Db.findFirst("SELECT * FROM tbtribe_member WHERE tribe_id=? AND user_id=?", tribe_id, user_id) == null) {
                controller.renderJson(new BaseResponse(Code.FAILURE, "you are not in this tribe"));// 你不在部落中~~
            } else {
                // 继续下去
                controller.setAttr("tribe", tribe);
                inv.invoke();
            }
        }
    }
}
