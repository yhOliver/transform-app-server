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
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.Favorite;
import transform.app.server.model.FeedBack;
import transform.app.server.model.User;

import static transform.app.server.model.Favorite.*;

/**
 * 公共模块的api*
 * <p>
 * 意见反馈: POST /api/feedback
 * 收藏:    POST /api/favorite
 * 取消收藏:    POST /api/cancelFavorite
 *
 * @author zhuqi259
 */
public class CommonAPIController extends BaseAPIController {
    /**
     * 处理用户意见反馈
     * <p>
     * POST、登陆状态、事务
     */
    @Before({POST.class, TokenInterceptor.class, Tx.class})
    public void feedback() {
        //内容
        String suggestion = getPara("suggestion");
        if (!notNull(Require.me()
                .put(suggestion, "suggestion can not be null"))) {
            return;
        }
        FeedBack feedBack = new FeedBack().set(FeedBack.SUGGESTION, suggestion)
                .set(FeedBack.CREATETIME, DateUtils.currentTimeStamp());
        User user = getUser();
        feedBack.set(FeedBack.USER_ID, user.userId());
        //保存反馈
        boolean flag = feedBack.save();
        renderJson(new BaseResponse(flag ? Code.SUCCESS : Code.FAILURE, flag ? "意见反馈成功" : "意见反馈失败"));
    }

    /**
     * TODO 收藏【暂时仅支持帖子收藏】
     * <p>
     * POST、登陆、事务
     */
    @Before({POST.class, TokenInterceptor.class, Tx.class})
    public void favorite() {
        User user = getUser();
        String user_id = user.userId();
        int collection_type = getParaToInt(COLLECTION_TYPE, 1);
        String fk_id = getPara(FK_ID);

        //校验必填项参数
        if (!notNull(Require.me()
                .put(collection_type, "collection_type can not be null")
                .put(fk_id, "fk_id can not be null"))) {
            return;
        }

        /**
         * 帖子等资源状态
         */
        if (collection_type == 1) {
            // 帖子
            Record post = Db.findFirst("SELECT * FROM tbpost WHERE post_id = ? AND status = 1", fk_id);
            if (post == null) {
                renderFailed("找不到你要收藏的资源");
                return;
            }
        }

        Record a = Db.findFirst("SELECT * FROM t_collection WHERE fk_id = ? AND user_id = ? AND collection_type = ?", fk_id, user_id, collection_type);
        if (a != null) {
            renderFailed("您不能重复收藏同一资源");
            return;
        }

        // 收藏
        Favorite favorite = new Favorite()
                .set(COLLECTION_ID, RandomUtils.randomCustomUUID())
                .set(USER_ID, user_id)
                .set(COLLECTION_TYPE, collection_type)
                .set(FK_ID, fk_id)
                .set(CREATETIME, DateUtils.currentTimeStamp());
        boolean saved = favorite.save();
        renderJson(new BaseResponse(saved ? Code.SUCCESS : Code.FAILURE, saved ? "收藏成功" : "收藏失败"));
    }

    /**
     * 取消收藏【暂时仅支持取消帖子收藏】
     * <p>
     * POST、登陆、事务
     */
    @Before({POST.class, TokenInterceptor.class, Tx.class})
    public void cancelFavorite() {
        String collection_id = getPara(COLLECTION_ID);
        if (StringUtils.isEmpty(collection_id)) {
            renderFailed("collection_id 不能为空");
            return;
        }

        User user = getUser();
        String user_id = user.userId();

        Favorite favorite = Favorite.dao.findById(collection_id);
        if (favorite == null || !user_id.equals(favorite.getStr(USER_ID))) {
            renderFailed("您要取消收藏的资源不存在 或者 您未收藏");
            return;
        }
        boolean deleted = favorite.delete();
        renderJson(new BaseResponse(deleted ? Code.SUCCESS : Code.FAILURE, deleted ? "取消收藏成功" : "取消收藏失败"));
    }
}
