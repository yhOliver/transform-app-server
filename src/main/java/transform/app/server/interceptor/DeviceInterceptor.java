package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;

import static transform.app.server.model.Distance.DEVICE_UUID;

/**
 * 设备ID拦截器
 * => 获取手机端设备生成的唯一UUID
 *
 * @author zhuqi259
 *         2016-3-22
 */
public class DeviceInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String device_uuid = controller.getPara(DEVICE_UUID);
        if (StringUtils.isEmpty(device_uuid)) {
            controller.renderJson(new BaseResponse(Code.ARGUMENT_ERROR, "device uuid can not be null"));
            return;
        }
        inv.invoke();
    }
}
