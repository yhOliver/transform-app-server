package yh.handler;

import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.jfinal.kit.HandlerKit;
import com.jfinal.render.RenderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;

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

    private static final Logger logger = LoggerFactory.getLogger(APINotFoundHandler.class);

    private final String prefix = "/jsp";
    private final String postfix = ".html";

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        response.setDateHeader("Expires", -1);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        if (!target.startsWith("/api")) {
            //handler 非接口访问
            //登录的页面不拦截
            if ("/login.html".equals(target)){
                next.handle(target, request, response, isHandled);
                return;
            }
            if ("/register.html".equals(target)){
                next.handle(target, request, response, isHandled);
                return;
            }
            if (target.trim().startsWith("/asserts")){
                next.handle(target, request, response, isHandled);
                return;
            }

            //访问其他的页面拦截
            if (target.trim().startsWith(prefix) && target.trim().endsWith(postfix)){
                if (request.getSession().getAttribute("username") != null){
                    /** already login **/
                    this.next.handle(target, request, response, isHandled);
                    return;
                }
            }
            //访问mp4文件
            if (target.trim().endsWith(".mp4")){
                if (request.getSession().getAttribute("username") != null){
                    /** already login **/
                    this.next.handle(target, request, response, isHandled);
                    return;
                }
            }
            HandlerKit.redirect("/login.html", request, response, isHandled);
            return;
        }
        //判断api访问是否合法
        if (JFinal.me().getAction(target, new String[1]) == null) {
            isHandled[0] = true;
            try {
                request.setCharacterEncoding("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            RenderFactory.me().getJsonRender(new BaseResponse(Code.FAILURE, "resource is not found!")).setContext(request, response).render();
        } else {
            this.next.handle(target, request, response, isHandled);
        }
    }

   /* private void watch(String target, HttpServletRequest request, HttpServletResponse response){
        int user_id = (Integer) request.getSession().getAttribute("username");
        Record record = Db.findFirst("select * from watch where user_id = ? and target = ?", user_id, target);
        if (record == null){
            File file = new File("/jsp/resources/resources/json");
            String str = file.toString();
            List<Resource> list = Jackson.getJson().parse(str, List.class);
           // Db.update("insert into watch (name, location, type, time, user_id, count) values (?, ?, ?, ?, ?, 1)", target.split("/")., location, type, DateUtils.currentTimeStamp(), user_id);
            logger.info("aaaaaaa");
        }else {
            Db.update("update watch set count = count + 1 where target = ? and user_id = ? ", target, user_id);
        }
    }*/
}
