package yh;

import com.jfinal.config.Routes;
import yh.api.*;

/**
 * @author malongbo
 */
public class APIRouter extends Routes {
    @Override
    public void config() {

        //文件相关
        add("/api/fs", FileAPIController.class);
        //用户相关
        add("/api/account", AccountAPIController.class);
        //课程相关
        add("/api/course", CourseAPIController.class);
        //资源相关
        add("/api/resource", ResourceAPIController.class);

    }
}
