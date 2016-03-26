package transform.app.server.router;

import com.jfinal.config.Routes;
import transform.app.server.api.*;

/**
 * @author malongbo
 */
public class APIRouter extends Routes {
    @Override
    public void config() {
        // TODO router
        //公共api
        add("/api", CommonAPIController.class);
        //文件相关
        add("/api/fs", FileAPIController.class);
        //用户相关
        add("/api/account", AccountAPIController.class);
        //场馆相关
        add("/api/venue", VenueAPIController.class);
        //部落相关
        add("/api/tribe", TribeAPIController.class);
        //帖子相关
        add("/api/post", PostAPIController.class);
    }
}
