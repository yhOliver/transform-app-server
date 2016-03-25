package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.interceptor.TribeInterceptor;
import transform.app.server.model.Tribe;
import transform.app.server.model.User;

import static transform.app.server.model.Tribe.*;
import static transform.app.server.model.User.UPDATETIME;


/**
 * 部落相关的接口*
 * <p>
 * 创建部落         POST /api/tribe/create
 * 更新部落信息     POST /api/tribe/update
 * 更新部落头像     POST /api/tribe/avatar
 * 查看部落信息     POST /api/tribe/view
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class TribeAPIController extends BaseAPIController {

    @Before(Tx.class)
    public void create() {
        User user = getUser();
        String tribe_name = getPara(TRIBE_NAME);
        String tribe_img = getPara(TRIBE_IMG);
        String tribe_info = getPara(TRIBE_INFO);

        //校验必填项参数
        if (!notNull(Require.me()
                .put(tribe_name, "tribe name can not be null")
                .put(tribe_img, "tribe img can not be null")
                .put(tribe_info, "tribe info can not be null"))) {
            return;
        }

        // 创建部落
        String tribe_id = RandomUtils.randomCustomUUID();
        Tribe tribe = new Tribe()
                .set(TRIBE_ID, tribe_id)
                .set(TRIBE_NAME, tribe_name)
                .set(TRIBE_IMG, tribe_img)
                .set(TRIBE_INFO, tribe_info)
                .set(USER_ID, user.get(User.USER_ID))
                .set(TRIBE_LEVEL, 1)
                .set(NUM_OF_MEMBERS, 1)
                .set(CREATETIME, DateUtils.currentTimeStamp())
                .set(UPDATETIME, DateUtils.currentTimeStamp());
        tribe.save();
        renderJson(new BaseResponse("create tribe success", tribe));
    }

    @Before({TribeInterceptor.class, Tx.class})
    public void update() {
        boolean flag = false;
        Tribe tribe = getAttr("tribe");
        String tribe_name = getPara(TRIBE_NAME);
        if (StringUtils.isNotEmpty(tribe_name)) {
            tribe.set(TRIBE_NAME, tribe_name);
            flag = true;
        }
        String tribe_img = getPara(TRIBE_IMG);
        if (StringUtils.isNotEmpty(tribe_img)) {
            tribe.set(TRIBE_IMG, tribe_img);
            flag = true;
        }
        String tribe_info = getPara(TRIBE_INFO);
        if (StringUtils.isNotEmpty(tribe_info)) {
            tribe.set(TRIBE_INFO, tribe_info);
            flag = true;
        }
        tribe.set(UPDATETIME, DateUtils.currentTimeStamp()); // 更新时间
        if (flag) {
            boolean update = tribe.update();
            BaseResponse response = new BaseResponse();
            // 不需要返回更新的部落数据（本地已经知道了）
            renderJson(response.setSuccess(update)
                    .setMsg(update ? "tribe update success" : "tribe update failed"));
        } else {
            renderArgumentError("must set profile of tribe");
        }

    }

    /**
     * 修改部落头像
     * /api/tribe/avatar
     */
    @Before({TribeInterceptor.class, Tx.class})
    public void avatar() {
        String avatar = getPara(TRIBE_IMG);
        if (!notNull(Require.me()
                .put(avatar, "avatar url can not be null"))) {
            return;
        }
        Tribe tribe = getAttr("tribe");
        boolean update = tribe.set(TRIBE_IMG, avatar)
                .set(UPDATETIME, DateUtils.currentTimeStamp()).update();
        renderJson(new BaseResponse().setSuccess(update)
                .setMsg(update ? "update tribe img success" : "update tribe img failed"));
    }

    /**
     * 部落信息查看 => 是个人就可以
     */
    @Clear
    @Before(POST.class)
    public void view() {
        String tribe_id = getPara(TRIBE_ID);
        if (StringUtils.isEmpty(tribe_id)) {
            renderJson(new BaseResponse(Code.FAILURE, "tribe id can not be null"));
            return;
        }
        Tribe tribe = Tribe.dao.findById(tribe_id);
        if (tribe == null) {
            renderJson(new BaseResponse(Code.FAILURE, "tribe is not found"));// 找不到部落
        } else {
            renderJson(new BaseResponse(tribe));
        }
    }
}