package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.FeedBack;
import transform.app.server.model.User;

/**
 * 公共模块的api*
 * <p>
 * 意见反馈: POST /api/feedback
 *
 * @author malongbo
 */
public class CommonAPIController extends BaseAPIController {
    /**
     * 处理用户意见反馈
     */
    @Before({TokenInterceptor.class, Tx.class})
    public void feedback() {
        if (!"post".equalsIgnoreCase(getRequest().getMethod())) {
            renderJson(new BaseResponse(Code.NOT_FOUND));
            return;
        }
        //内容
        String suggestion = getPara("suggestion");
        if (!notNull(Require.me()
                .put(suggestion, "suggestion can not be null"))) {
            return;
        }
        FeedBack feedBack = new FeedBack().set(FeedBack.SUGGESTION, suggestion)
                .set(FeedBack.CREATION_DATE, DateUtils.getNowTimeStamp());
        User user = getUser();
        if (user != null) {
            feedBack.set(FeedBack.USER_ID, user.userId());
        }
        //保存反馈
        boolean flag = feedBack.save();
        renderJson(new BaseResponse(flag ? Code.SUCCESS : Code.FAIL, flag ? "意见反馈成功" : "意见反馈失败"));
    }
}
