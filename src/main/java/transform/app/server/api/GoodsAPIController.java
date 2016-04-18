package transform.app.server.api;

import com.jfinal.aop.Before;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.POST;
import transform.app.server.model.GoodsCategory;

import java.util.List;

import static transform.app.server.model.GoodsCategory.FATHER_ID;

/**
 * 商城相关接口
 * <p>
 * 查看类别   POST /api/goods/categories
 *
 * @author zhuqi259
 */
@Before(POST.class)
public class GoodsAPIController extends BaseAPIController {

    /**
     * 查看类别
     */
    public void categories() {
        String father_id = getPara(FATHER_ID);
        if (StringUtils.isEmpty(father_id)) {
            // 查看大类别
            List<GoodsCategory> goodsCategories = GoodsCategory.dao.find("SELECT * FROM tbgoods_catagory WHERE father_id IS NULL");
            renderJson(new BaseResponse(Code.SUCCESS, "", goodsCategories));
        } else {
            // 查看小类别
            List<GoodsCategory> goodsCategories = GoodsCategory.dao.find("SELECT * FROM tbgoods_catagory WHERE father_id=?", father_id);
            renderJson(new BaseResponse(Code.SUCCESS, "", goodsCategories));
        }
    }
}
