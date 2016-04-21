package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.*;
import transform.app.server.model.Tribe;
import transform.app.server.model.TribeMember;
import transform.app.server.model.User;

import static transform.app.server.model.Tribe.*;


/**
 * 部落相关的接口*
 * <p>
 * 创建部落         POST /api/tribe/create
 * 更新部落信息     POST /api/tribe/update
 * 更新部落头像     POST /api/tribe/avatar
 * 查看部落信息     POST /api/tribe/view
 * 加入部落         POST /api/tribe/join
 * 退出部落         POST /api/tribe/leave
 * 我的部落         POST /api/tribe/mine
 * 其他部落         POST /api/tribe/others
 * 部落成员         POST /api/tribe/members
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class TribeAPIController extends BaseAPIController {
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 创建部落 => 登陆会员就可以
     * <p>
     * POST、登陆状态、事务
     */
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
        renderJson(new BaseResponse(Code.SUCCESS, "create tribe success", tribe));
    }

    /**
     * 更新部落信息 => 部落创建者
     * <p>
     * POST、登陆状态、部落创建者、事务
     */
    @Before({TribeOwnerInterceptor.class, Tx.class})
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
            renderJson(response.setSuccess(update ? Code.SUCCESS : Code.FAILURE)
                    .setMsg(update ? "tribe update success" : "tribe update failed"));
        } else {
            renderArgumentError("must set profile of tribe");
        }

    }

    /**
     * 修改部落头像
     * /api/tribe/avatar
     * => 部落创建者
     * <p>
     * POST、登陆状态、部落创建者、事务
     */
    @Before({TribeOwnerInterceptor.class, Tx.class})
    public void avatar() {
        String avatar = getPara(TRIBE_IMG);
        if (!notNull(Require.me()
                .put(avatar, "avatar url can not be null"))) {
            return;
        }
        Tribe tribe = getAttr("tribe");
        boolean update = tribe.set(TRIBE_IMG, avatar)
                .set(UPDATETIME, DateUtils.currentTimeStamp()).update();
        renderJson(new BaseResponse().setSuccess(update ? Code.SUCCESS : Code.FAILURE)
                .setMsg(update ? "update tribe img success" : "update tribe img failed"));
    }

    /**
     * 部落信息查看 => 是个人就可以
     * <p>
     * POST、检查部落存在
     */
    @Clear
    @Before({POST.class, TribeStatusInterceptor.class})
    public void view() {
        //  Tribe tribe = getAttr("tribe");
        String tribe_id = getPara(TRIBE_ID);
        // 登陆状态 与 非登陆状态
        String token = getPara("token");
        if (StringUtils.isNotEmpty(token)) {
            User user = TokenManager.getMe().validate(token);
            if (user == null) {
                renderFailed("token is invalid");
                return;
            }
            String user_id = user.userId();
            // 登陆状态
            /**
             SELECT tt.*, tu.user_nickname, tu.user_photo, tu.user_signature, (CASE WHEN tt2.tribe_id IS NULL THEN 0 ELSE 1 END) AS join_status
             FROM (SELECT * FROM tbtribe WHERE tribe_id = ?) tt LEFT JOIN (SELECT tribe_id FROM tbtribe WHERE user_id = ? OR tribe_id IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?)) tt2 ON tt.tribe_id = tt2.tribe_id
             LEFT JOIN tbuser tu ON tt.user_id = tu.user_id
             */
            Record tribe = Db.findFirst("SELECT tt.*, tu.user_nickname, tu.user_photo, tu.user_signature, (CASE WHEN tt2.tribe_id IS NULL THEN 0 ELSE 1 END) AS join_status " +
                    "FROM (SELECT * FROM tbtribe WHERE tribe_id = ?) tt LEFT JOIN (SELECT tribe_id FROM tbtribe WHERE user_id = ? OR tribe_id IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?)) tt2 ON tt.tribe_id = tt2.tribe_id " +
                    "LEFT JOIN tbuser tu ON tt.user_id = tu.user_id", tribe_id, user_id, user_id);
            renderJson(new BaseResponse(Code.SUCCESS, "", tribe));
        } else {
            // 未登陆
            /**
             SELECT tt.*, tu.user_nickname, tu.user_photo, tu.user_signature, 0 AS join_status FROM (SELECT * FROM tbtribe WHERE tribe_id = ?) tt LEFT JOIN tbuser tu ON tt.user_id = tu.user_id
             */
            Record tribe = Db.findFirst("SELECT tt.*, tu.user_nickname, tu.user_photo, tu.user_signature, 0 AS join_status FROM (SELECT * FROM tbtribe WHERE tribe_id = ?) tt LEFT JOIN tbuser tu ON tt.user_id = tu.user_id", tribe_id);
            renderJson(new BaseResponse(Code.SUCCESS, "", tribe));
        }
    }

    /**
     * 加入部落 =>登陆状态
     * <p>
     * POST、登陆状态、检查部落存在、事务
     */
    @Before({TribeStatusInterceptor.class, Tx.class})
    public void join() {
        User user = getUser();
        String user_id = user.userId();
        Tribe tribe = getAttr("tribe");
        String tribe_id = getPara(TRIBE_ID);
        if (user_id.equals(tribe.getStr(USER_ID))) {
            // 创建者
            renderFailed("you are the creator, you need not join");
            return;
        }
        if (Db.findFirst("SELECT * FROM tbtribe_member WHERE tribe_id=? AND user_id=?", tribe_id, user_id) != null) {
            renderFailed("you have already joined");
            return;
        }
        // 新增部落成员表记录
        boolean saved = new TribeMember()
                .set(TribeMember.ID, RandomUtils.randomCustomUUID())
                .set(TribeMember.TRIBE_ID, tribe_id)
                .set(TribeMember.USER_ID, user_id)
                .set(TribeMember.OCCURRENCE_TIME, DateUtils.currentTimeStamp())
                .save();
        if (saved) {
            // 部落人数+1
            Db.update("UPDATE tbtribe SET num_of_members = num_of_members+1 WHERE tribe_id = ?", tribe_id);
            renderSuccess("join success");
        } else {
            renderFailed("join failed");
        }
    }

    /**
     * 退出部落(登陆状态，是部落成员，且不是创建者)
     * <p>
     * POST、登陆状态、检查是否是部落成员（包括了检查部落存在）、事务
     */
    @Before({TribeMemberInterceptor.class, Tx.class})
    public void leave() {
        User user = getUser();
        String user_id = user.userId();
        Tribe tribe = getAttr("tribe");
        String tribe_id = getPara(TRIBE_ID);
        if (user_id.equals(tribe.getStr(USER_ID))) {
            // 创建者
            renderFailed("you are the creator, you can not leave");
            return;
        }
        // 删除部落成员表记录
        int deleted = Db.update("DELETE FROM tbtribe_member WHERE tribe_id=? AND user_id=?", tribe_id, user_id);
        if (deleted > 0) {
            // 部落人数-1
            Db.update("UPDATE tbtribe SET num_of_members = num_of_members-1 WHERE tribe_id = ?", tribe_id);
            renderSuccess("leave the tribe success");
        } else {
            renderFailed("leave the tribe failed");
        }
    }

    /**
     * 我的部落（我创建的与我加入的）
     * <p>
     * POST、登陆状态
     */
    public void mine() {
        User user = getUser();
        String user_id = user.userId();
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         SELECT *
         FROM tbtribe WHERE user_id = ? OR tribe_id IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?) ORDER BY createtime DESC
         */
        Page<Tribe> tribes = Tribe.dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM tbtribe WHERE user_id = ? OR tribe_id IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?) ORDER BY createtime DESC", user_id, user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", tribes));
    }


    /**
     * 其他部落 （非我创建的且未加入）
     * <p>
     * POST、登陆状态
     */
    public void others() {
        User user = getUser();
        String user_id = user.userId();
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         SELECT *
         FROM tbtribe WHERE (user_id <> ? OR user_id IS NULL) AND tribe_id NOT IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?) ORDER BY createtime DESC
         */
        Page<Tribe> tribes = Tribe.dao.paginate(pageNumber, pageSize, "SELECT * ", "FROM tbtribe WHERE (user_id <> ? OR user_id IS NULL) AND tribe_id NOT IN (SELECT tribe_id FROM tbtribe_member WHERE user_id = ?) ORDER BY createtime DESC", user_id, user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", tribes));
    }


    /**
     * 部落成员列表 [成员(包括创建者)]
     * <p>
     * POST
     */
    @Clear
    @Before({POST.class, TribeStatusInterceptor.class})
    public void members() {
        String tribe_id = getPara(TRIBE_ID);
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        /**
         SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature, tm.owner_status
         FROM (SELECT user_id, 1 AS owner_status FROM tbtribe WHERE tribe_id = ? UNION SELECT user_id, 0 AS owner_status FROM tbtribe_member WHERE tribe_id = ?) tm LEFT JOIN tbuser tu ON tm.user_id = tu.user_id
         */
        Page<Record> tribe_members = Db.paginate(pageNumber, pageSize, "SELECT tu.user_id, tu.user_nickname, tu.user_photo, tu.user_signature, tm.owner_status",
                "FROM (SELECT user_id, 1 AS owner_status FROM tbtribe WHERE tribe_id = ? UNION SELECT user_id, 0 AS owner_status FROM tbtribe_member WHERE tribe_id = ?) tm LEFT JOIN tbuser tu ON tm.user_id = tu.user_id", tribe_id, tribe_id);
        renderJson(new BaseResponse(Code.SUCCESS, "", tribe_members));
    }
}