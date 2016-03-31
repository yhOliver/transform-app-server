package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.bean.BaseResponse;
import transform.app.server.common.bean.Code;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.FeedBack;
import transform.app.server.model.User;

/**
 * 公共模块的api*
 * <p>
 * 意见反馈: POST /api/feedback
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
}
