package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.ConsigneeOwnerInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.ConsigneeAddress;
import transform.app.server.model.GoodsCategory;
import transform.app.server.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.scenario.Settings.set;
import static transform.app.server.model.ConsigneeAddress.*;
import static transform.app.server.model.ConsigneeAddress.CONSIGNEE_TEL;
import static transform.app.server.model.GoodsCategory.FATHER_ID;

/**
 * 收货地址相关接口
 * <p>
 * 我的收货地址 POST /api/consignee/mine
 * 查看收货地址 POST /api/consignee/view
 * 增加收货地址 POST /api/consignee/add
 * 删除收货地址 POST /api/consignee/del
 * 修改收货地址 POST /api/consignee/update
 * 设置默认收货地址 POST /api/consignee/defaultAddress
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
         * SELECT consignee_id, address, zipcode, consignee_tel, consignee_name, status FROM tbconsignee_address WHERE user_id = ? ORDER BY status DESC
         */
        List<Record> addresses = Db.find("SELECT consignee_id, address, zipcode, consignee_tel, consignee_name, status FROM tbconsignee_address WHERE user_id = ? ORDER BY status DESC", user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", addresses));
    }

    /**
     * 查看收货地址 POST /api/consignee/view
     * <p>
     * POST、登陆状态、拥有者权限【只能查看自己的收货地址】
     */
    @Before(ConsigneeOwnerInterceptor.class)
    public void view() {
        ConsigneeAddress consigneeAddress = getAttr("consigneeAddress");
        Map<String, Object> info = new HashMap<>(consigneeAddress.getAttrs());
        info.remove(USER_ID);
        info.remove(CREATETIME);
        info.remove(UPDATETIME);
        renderJson(new BaseResponse(Code.SUCCESS, "", info));

//        String consignee_id = getPara(CONSIGNEE_ID);
//        Record consigneeAddress = Db.findFirst("SELECT consignee_id, address, zipcode, consignee_tel, consignee_name, flag FROM tbconsignee_address WHERE consignee_id = ?", consignee_id);
//        if (consigneeAddress == null) {
//            renderFailed("收货地址不存在");
//        } else {
//            renderJson(new BaseResponse(Code.SUCCESS, "", consigneeAddress));
//        }
    }

    /**
     * 增加收货地址 POST /api/consignee/add
     * <p>
     * POST、登陆状态、事务
     */
    @Before(Tx.class)
    public void add() {
        User user = getUser();
        String user_id = user.userId(); // 用户ID

        String address = getPara(ADDRESS);
        String zipcode = getPara(ZIPCODE);
        String consignee_name = getPara(CONSIGNEE_NAME);
        String consignee_tel = getPara(CONSIGNEE_TEL);

        //校验必填项参数
        if (!notNull(Require.me()
                .put(address, "conignee address can not be null")
                .put(zipcode, "zipcode can not be null")
                .put(consignee_name, "consignee name can not be null")
                .put(consignee_tel, "consignee tel can not be null"))) {
            return;
        }

        String consignee_id = RandomUtils.randomCustomUUID();
        ConsigneeAddress consigneeAddress = new ConsigneeAddress()
                .set(CONSIGNEE_ID, consignee_id)
                .set(USER_ID, user_id)
                .set(ADDRESS, address)
                .set(ZIPCODE, zipcode)
                .set(CONSIGNEE_TEL, consignee_tel)
                .set(CONSIGNEE_NAME, consignee_name)
                .set(CREATETIME, DateUtils.currentTimeStamp())
                .set(UPDATETIME, DateUtils.currentTimeStamp())
                .set(STATUS, 0);
        boolean saved = consigneeAddress.save();
        if (saved) {
            renderSuccess("添加地址成功");
        } else {
            renderFailed("添加地址失败");
        }
    }

    /**
     * 删除收货地址
     * <p>
     * post、登陆状态、事务、拥有者权限【只能删除自己的收货地址】
     */
    @Before({Tx.class, ConsigneeOwnerInterceptor.class})
    public void del() {
        ConsigneeAddress consigneeAddress = getAttr("consigneeAddress");
        boolean deleted = consigneeAddress.delete();
        if (deleted) {
            renderSuccess("删除收货地址成功");
        } else {
            renderFailed("删除收货地址失败");
        }

//        String consignee_id = getPara(CONSIGNEE_ID);
//        if (StringUtils.isEmpty(consignee_id)) {
//            renderFailed("consignee id can not be null");
//            return;
//        }
//        User user = getUser();
//        String user_id = user.userId(); // 用户ID
//        // 用户只能删自己的收货地址
//        int deleted = Db.update("DELETE FROM tbconsignee_address WHERE consignee_id=? AND user_id=?", consignee_id, user_id);
//        if(deleted > 0){
//            renderSuccess("删除收货地址成功");
//        }else{
//            renderFailed("删除收货地址失败");
//        }
    }

    /**
     * 修改收货地址
     * <p>
     * post、登陆状态、事务、拥有者权限【只能修改自己的收货地址】
     */
    @Before({Tx.class, ConsigneeOwnerInterceptor.class})
    public void update() {
        boolean flag = false;
        BaseResponse response = new BaseResponse();
//        String consignee_id = getPara(CONSIGNEE_ID);
//        if (StringUtils.isEmpty(consignee_id)) {
//            renderFailed("consignee id can not be null");
//            return;
//        }
//        ConsigneeAddress consigneeAddress = ConsigneeAddress.dao.findById(consignee_id);
//        if (consigneeAddress == null) {
//            renderFailed("收货地址不存在");
//            return;
//        }
//        // 当前用户修改自己的收货地址

        ConsigneeAddress consigneeAddress = getAttr("consigneeAddress");

        String address = getPara(ADDRESS);
        if (StringUtils.isNotEmpty(address)) {
            consigneeAddress.set(ADDRESS, address);
            flag = true;
        }
        String zipcode = getPara(ZIPCODE);
        if (StringUtils.isNotEmpty(zipcode)) {
            consigneeAddress.set(ZIPCODE, zipcode);
            flag = true;
        }
        String tel = getPara(CONSIGNEE_TEL);
        if (StringUtils.isNotEmpty(tel)) {
            consigneeAddress.set(CONSIGNEE_TEL, tel);
            flag = true;
        }
        String name = getPara(CONSIGNEE_NAME);
        if (StringUtils.isNotEmpty(name)) {
            consigneeAddress.set(CONSIGNEE_NAME, name);
            flag = true;
        }
        consigneeAddress.set(UPDATETIME, DateUtils.currentTimeStamp()); // 更新时间
        if (flag) {
            boolean updated = consigneeAddress.update();
            renderJson(response.setSuccess(updated ? Code.SUCCESS : Code.FAILURE).setMsg(updated ? "修改收货地址成功" : "修改收货地址失败"));
        } else {
            renderArgumentError("必须至少修改一项");
        }
    }

    /**
     * 设置默认收货地址
     * <p>
     * post、登陆状态、事务、拥有者权限【只能设置自己的默认收货地址】
     */
    @Before({Tx.class, ConsigneeOwnerInterceptor.class})
    public void defaultAddress() {
        // 1.把该用户的默认地址清0
        User user = getAttr("user");
        String user_id = user.userId();
        Db.update("UPDATE tbconsignee_address SET status = 0 WHERE user_id = ? AND status = 1", user_id);

        // 2. 设置默认地址
        ConsigneeAddress consigneeAddress = getAttr("consigneeAddress");
        consigneeAddress.set(STATUS, 1);
        boolean updated = consigneeAddress.update();
        renderJson(new BaseResponse().setSuccess(updated ? Code.SUCCESS : Code.FAILURE).setMsg(updated ? "设置默认收货地址成功" : "设置默认收货地址失败"));
    }
}
