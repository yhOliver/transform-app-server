package transform.app.server.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Post;

/**
 * 帖子拦截器
 *
 * @author zhuqi259
 *         2016-3-19
 */
public class PostStatusInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String post_id = controller.getPara(Post.POST_ID);
        if (StringUtils.isEmpty(post_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "post id can not be null"));
            return;
        }
        // 帖子信息（包括所属部落名称）
        Record post = Db.findFirst("SELECT tp.*, tt.tribe_name FROM ( SELECT * FROM tbpost WHERE post_id = ? AND status = 1 ) tp LEFT JOIN tbtribe tt ON tp.tribe_id = tt.tribe_id", post_id);
        if (post == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "post is not found"));// 找不到帖子
        } else {
            controller.setAttr("post", post);
            inv.invoke();
        }
    }
}
