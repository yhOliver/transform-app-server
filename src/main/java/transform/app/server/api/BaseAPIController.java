package transform.app.server.api;

import com.jfinal.core.Controller;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.User;

import java.lang.reflect.Array;

/**
 * 基本的api
 * 基于jfinal controller做一些封装
 *
 * @author malongbo
 */
public class BaseAPIController extends Controller {

    /**
     * 获取当前用户对象
     *
     * @return User
     */
    protected User getUser() {
        User user = getAttr("user");
        if (user == null) {
            String token = getPara("token");
            return StringUtils.isEmpty(token) ? null : TokenManager.getMe().validate(token);
        }
        return getAttr("user");
    }

    /**
     * 响应接口不存在*
     */
    public void render404() {
        renderJson(new BaseResponse(Code.FAILURE, "接口不存在"));
    }

    /**
     * 响应请求参数有误*
     *
     * @param msg 错误信息
     */
    public void renderArgumentError(String msg) {
        renderJson(new BaseResponse(Code.FAILURE, msg));
    }


    /**
     * 响应操作成功*
     *
     * @param msg 响应信息
     */
    public void renderSuccess(String msg) {
        renderJson(new BaseResponse().setMsg(msg));
    }

    /**
     * 响应操作失败*
     *
     * @param msg 响应信息
     */
    public void renderFailed(String msg) {
        renderJson(new BaseResponse(Code.FAILURE, msg));
    }

    /**
     * 判断参数值是否为空
     *
     * @param rules Require
     * @return boolean
     */
    public boolean notNull(Require rules) {
        if (rules == null || rules.getLength() < 1) {
            return true;
        }
        for (int i = 0, total = rules.getLength(); i < total; i++) {
            Object key = rules.get(i);
            String msg = rules.getMessage(i);
            BaseResponse response = new BaseResponse(Code.FAILURE);
            if (key == null) {
                renderJson(response.setMsg(msg));
                return false;
            }
            if (key instanceof String && StringUtils.isEmpty((String) key)) {
                renderJson(response.setMsg(msg));
                return false;
            }
            if (key instanceof Array) {
                Object[] arr = (Object[]) key;
                if (arr.length < 1) {
                    renderJson(response.setMsg(msg));
                    return false;
                }
            }
        }
        return true;
    }
}
