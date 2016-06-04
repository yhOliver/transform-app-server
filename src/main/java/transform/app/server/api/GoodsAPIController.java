package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.bean.GoodsDetailVO;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.GoodsStatusInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.model.Goods;
import transform.app.server.model.GoodsCategory;

import java.util.List;

import static transform.app.server.model.Goods.CATA_PARENT_ID;
import static transform.app.server.model.Goods.GOODS_ID;
import static transform.app.server.model.GoodsCategory.FATHER_ID;

/**
 * 商城相关接口
 * <p>
 * 查看类别           POST /api/goods/categories
 * 查看商品列表       POST /api/goods/list
 * 查看商品详细信息   POST /api/goods/view
 * 查看商品评价      POST /api/goods/comments
 *
 * @author zhuqi259
 */
@Before(POST.class)
public class GoodsAPIController extends BaseAPIController {
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 查看类别
     * <p>
     * POST
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
     * <p>
     * POST
     */
    public void list() {
        String cata_parent_id = getPara(CATA_PARENT_ID);
        if (StringUtils.isEmpty(cata_parent_id)) {
            renderFailed("cata_parent_id 不能为空");
            return;
        }
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber);//页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         * SELECT goods_id, goods_name, goods_price, goods_gallery
         * FROM tbgoods WHERE cata_parent_id=?
         */
        Page<Record> goods = Db.paginate(pageNumber, pageSize, "SELECT goods_id, goods_name, goods_price, goods_gallery",
                "FROM tbgoods WHERE cata_parent_id=?", cata_parent_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", goods));
    }

    /**
     * 查看商品详细信息 POST /api/goods/view
     * <p>
     * POST、商品状态
     */
    @Before(GoodsStatusInterceptor.class)
    public void view() {
        Goods goods = getAttr("goods");
        String goods_id = goods.getStr(GOODS_ID);

        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setDetailedInfo(goods);

        /**
         * 获取该商品的属性信息 [1. 商品特有属性，2. 商品类别具有的属性]
         *
         * SELECT tk.attr_key_name, tv.attr_value_name
         * FROM ( (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk WHERE tgk.goods_id = '111')
         * UNION (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk LEFT JOIN (SELECT cata_id FROM tbgoods WHERE goods_id = '111') tg ON tgk.cata_id = tg.cata_id WHERE tg.cata_id IS NOT NULL)
         * UNION (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk LEFT JOIN (SELECT cata_parent_id FROM tbgoods WHERE goods_id = '111') tg ON tgk.cata_id = tg.cata_parent_id WHERE tg.cata_parent_id IS NOT NULL)) tk
         * LEFT JOIN (SELECT attr_value_id, attr_value_name, attr_key_id FROM tbgoods_attr_value WHERE goods_id = '111') tv ON tk.attr_key_id = tv.attr_key_id
         * WHERE tv.attr_value_id IS NOT NULL
         */
        String sql = "SELECT tk.attr_key_name, tv.attr_value_name" +
                " FROM ( (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk WHERE tgk.goods_id = ?)" +
                " UNION (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk LEFT JOIN (SELECT cata_id FROM tbgoods WHERE goods_id = ?) tg ON tgk.cata_id = tg.cata_id WHERE tg.cata_id IS NOT NULL)" +
                " UNION (SELECT tgk.attr_key_id, tgk.attr_key_name FROM tbgoods_attr_key tgk LEFT JOIN (SELECT cata_parent_id FROM tbgoods WHERE goods_id = ?) tg ON tgk.cata_id = tg.cata_parent_id WHERE tg.cata_parent_id IS NOT NULL)) tk" +
                " LEFT JOIN (SELECT attr_value_id, attr_value_name, attr_key_id FROM tbgoods_attr_value WHERE goods_id = ?) tv ON tk.attr_key_id = tv.attr_key_id" +
                " WHERE tv.attr_value_id IS NOT NULL";
        List<Record> attrs = Db.find(sql, goods_id, goods_id, goods_id, goods_id);
        vo.setAttrs(attrs);
        renderJson(new BaseResponse(Code.SUCCESS, "", vo));
    }


    /**
     * 查看商品评价      POST /api/goods/comments
     * <p>
     * POST、商品状态
     */
    @Before(GoodsStatusInterceptor.class)
    public void comments() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber);//页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        Goods goods = getAttr("goods");
        String goods_id = goods.getStr(Goods.GOODS_ID);
        /**
         * SELECT tc.*, tu.user_nickname, tu.user_photo
         * FROM (SELECT * FROM tbgoods_comment WHERE goods_id = ?) tc LEFT JOIN tbuser tu ON tc.user_id = tu.user_id ORDER BY tc.createtime
         */
        Page<Record> goodsComment = Db.paginate(pageNumber, pageSize, "SELECT tc.*, tu.user_nickname, tu.user_photo",
                "FROM (SELECT * FROM tbgoods_comment WHERE goods_id = ?) tc LEFT JOIN tbuser tu ON tc.user_id = tu.user_id ORDER BY tc.createtime", goods_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", goodsComment));
    }
}
