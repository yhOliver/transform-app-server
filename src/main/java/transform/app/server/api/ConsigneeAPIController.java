package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.GoodsCategory;
import transform.app.server.model.User;

import java.util.List;

import static transform.app.server.model.GoodsCategory.FATHER_ID;

/**
 * 收获地址相关接口
 * <p>
 * 我的收货地址 POST /api/consignee/mine
 * 查看收货地址 POST /api/consignee/view
 * 增加收货地址 POST /api/consignee/add
 * 删除收货地址 POST /api/consignee/del
 * 修改收货地址 POST /api/consignee/update
 * 设置默认收货地址 POST /api/consignee/default
 */
@Before({POST.class, TokenInterceptor.class})
public class ConsigneeAPIController extends BaseAPIController {

    /**
     * 我的收货地址 POST /api/consignee/mine
     * <p>
     * POST、登陆状态
     */
    public void mine() {
        User user = getUser();
        String user_id = user.userId(); // 用户ID
        /**
         * SELECT consignee_id, address, zipcode, consignee_tel, consignee_name, flag FROM tbconsignee_address WHERE user_id = ? ORDER BY flag DESC
         */
        List<Record> addresses = Db.find("SELECT consignee_id, address, zipcode, consignee_tel, consignee_name, flag FROM tbconsignee_address WHERE user_id = ? ORDER BY flag DESC", user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", addresses));
    }

    /**
     * 查看收货地址 POST /api/consignee/view
     * <p>
     * POST、登陆状态
     */
    public void view() {

    }

    /**
     * 增加收货地址 POST /api/consignee/add
     * <p>
     * POST、登陆状态、事务
     */
    @Before(Tx.class)
    public void add() {

    }

}
