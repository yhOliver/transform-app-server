package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.POST;
import transform.app.server.model.GoodsCategory;

import java.util.List;

import static transform.app.server.model.Goods.CATA_PARENT_ID;
import static transform.app.server.model.Goods.GOODS_ID;
import static transform.app.server.model.GoodsCategory.FATHER_ID;

/**
 * 商城相关接口
 * <p>
 * 查看类别          POST /api/goods/categories
 * 查看商品列表       POST /api/goods/list
 * 查看商品详细信息   POST /api/goods/view
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


    /**
     * 查看商品列表 POST /api/goods/list
     */
    public void list() {
        String cata_parent_id = getPara(CATA_PARENT_ID);
        if (StringUtils.isEmpty(cata_parent_id)) {
            renderFailed("cata_parent_id 不能为空");
            return;
        }
        /**
         * SELECT goods_id, goods_name, goods_price, goods_gallery FROM tbgoods WHERE cata_parent_id=?
         */
        List<Record> goods = Db.find("SELECT goods_id, goods_name, goods_price, goods_gallery FROM tbgoods WHERE cata_parent_id=?", cata_parent_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", goods));
    }

    /**
     * 查看商品详细信息 POST /api/goods/view
     */
    public void view() {
        String goods_id = getPara(GOODS_ID);
        List<Record> detailedInfo = Db.find("");
    }
}
