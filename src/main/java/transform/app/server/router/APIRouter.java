package transform.app.server.router;

import com.jfinal.config.Routes;
import transform.app.server.api.AccountAPIController;
import transform.app.server.api.CommonAPIController;
import transform.app.server.api.VenueAPIController;

/**
 * @author malongbo
 */
public class APIRouter extends Routes {
    @Override
    public void config() {
        // TODO router
        //公共api
        add("/api", CommonAPIController.class);
        //用户相关
        add("/api/account", AccountAPIController.class);
        //场馆相关
        add("/api/venue", VenueAPIController.class);
        
//        //文件相关
//        add("/api/fs",FileAPIController.class);

    }
}
