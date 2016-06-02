package transform.app.server.interceptor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.model.Goods;

import static transform.app.server.model.Goods.GOODS_ID;

/**
 * 商品状态拦截器
 */
public class GoodsStatusInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String goods_id = controller.getPara(GOODS_ID);
        if (StringUtils.isEmpty(goods_id)) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "goods id can not be null"));
            return;
        }
        // 已发布的该类别场馆
        Goods goods = Goods.dao.findFirst("SELECT * FROM tbgoods WHERE goods_id=? AND goods_isonsale=1", goods_id);
        if (goods == null) {
            controller.renderJson(new BaseResponse(Code.FAILURE, "goods is not existed or offline"));
        } else {
            // 继续下去
            controller.setAttr("goods", goods);
            inv.invoke();
        }
    }
}

