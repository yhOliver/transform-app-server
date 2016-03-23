package transform.app.server.handler;

import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.jfinal.render.RenderFactory;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 处理404接口*
 *
 * @author zhuqi259
 *         2016-03-23
 */
public class APINotFoundHandler extends Handler {
    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        if (!target.startsWith("/api")) {
            this.next.handle(target, request, response, isHandled);
            return;
        }

        if (JFinal.me().getAction(target, new String[1]) == null) {
            isHandled[0] = true;
            try {
                request.setCharacterEncoding("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            RenderFactory.me().getJsonRender(new BaseResponse(Code.FAILURE, "resource is not found")).setContext(request, response).render();
        } else {
            this.next.handle(target, request, response, isHandled);
        }
    }
}
