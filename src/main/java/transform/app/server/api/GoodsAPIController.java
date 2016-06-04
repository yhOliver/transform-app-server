package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.bean.GoodsDetailVO;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.GoodsStatusInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.Goods;
import transform.app.server.model.GoodsCategory;
import transform.app.server.model.GoodsComment;
import transform.app.server.model.User;

import java.util.Arrays;
import java.util.List;

import static transform.app.server.model.Goods.CATA_PARENT_ID;
import static transform.app.server.model.Goods.GOODS_ID;
import static transform.app.server.model.GoodsCategory.FATHER_ID;
import static transform.app.server.model.GoodsComment.*;

/**
 * 商城相关接口
 * <p>
 * 查看类别           POST /api/goods/categories
 * 查看商品列表       POST /api/goods/list
 * 查看商品详细信息   POST /api/goods/view
 * 查看商品评价      POST /api/goods/comments
 * <p>
 * 商品确认收货      POST /api/goods/confirmReceipt
 * 评价商品         POST /api/goods/comment
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

    /**
     * TODO 商品确认收货      POST /api/goods/confirmReceipt
     */
    public void confirmReceipt() {

    }


    /**
     * 评价商品        POST /api/goods/comment
     * <p>
     * POST、用户登陆状态、商品状态、事务
     */
    @Before({TokenInterceptor.class, GoodsStatusInterceptor.class, Tx.class})
    public void comment() {
        // 添加评论
        User user = getUser();
        String user_id = user.userId();
        Goods goods = getAttr("goods");
        String goods_id = goods.getStr(GOODS_ID);

        String order_id = getPara(ORDER_ID);
        String comment = getPara(COMMENT);
        Integer starlevel = getParaToInt(STARLEVEL, 1);
        String goods_spec = getPara(GOODS_SPEC); // 选填
        String mediaurl = getPara(MEDIAURL); // 晒图可选

        //校验必填项参数
        if (!notNull(Require.me()
                .put(order_id, "order_id can not be null")
                .put(comment, "comment can not be null")
                .put(starlevel, "starlevel can not be null"))) {
            return;
        }

        Integer[] stars = {1, 0, -1};
        if (!Arrays.asList(stars).contains(starlevel)) {
            renderFailed("starlevel must be 1 or 0 or -1");
            return;
        }

        // TODO order 确认收货判断 [需要查看订单表确认该商品已经确认收货]

        Record a = Db.findFirst("SELECT * FROM tbgoods_comment WHERE order_id = ? AND goods_id = ? AND user_id = ?", order_id, goods_id, user_id);
        if (a != null) {
            renderFailed("你不能重复评价同一订单中的同一商品");
            return;
        }

        GoodsComment goodsComment = new GoodsComment()
                .set(GOCO_ID, RandomUtils.randomCustomUUID())
                .set(GoodsComment.GOODS_ID, goods_id)
                .set(ORDER_ID, order_id)
                .set(USER_ID, user_id)
                .set(COMMENT, comment)
                .set(CREATETIME, DateUtils.currentTimeStamp())
                .set(STARLEVEL, starlevel)
                .set(GOODS_SPEC, goods_spec)
                .set(MEDIAURL, mediaurl);
        boolean saved = goodsComment.save();
        if (saved) {
            // 更新商品评价数=>  评论成功，商品评论数+1
            Db.update("UPDATE tbgoods SET num_of_comments = num_of_comments+1 WHERE goods_id = ?", goods_id);
            renderSuccess("商品评价成功");
        } else {
            renderFailed("商品评价失败");
        }
    }
}
