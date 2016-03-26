package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.FileUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.interceptor.CardInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.Card;
import transform.app.server.model.CardMedia;

import java.util.ArrayList;
import java.util.List;

import static transform.app.server.model.Card.*;
import static transform.app.server.model.CardMedia.*;


/**
 * 帖子相关的接口*
 * <p>
 * 发帖:                           POST /api/card/add
 * 回复:                           POST /api/card/reply
 * 查看部落内帖子列表（分页）:     POST /api/card/cards
 * 帖子详情:                       POST /api/venue/detail
 * 帖子回复更多分页:               POST /api/venue/comments
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class, CardInterceptor.class})
public class CardAPIController extends BaseAPIController {
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    @Before(Tx.class)
    public void add() {
        String device_name = getPara(DEVICE_NAME);
        //校验必填项参数
        if (!notNull(Require.me()
                .put(device_name, "device name can not be null"))) {
            return;
        }
        String card_content = getPara(CARD_CONTENT, "");
        // 上传文件，调用文件上传接口 (已经上传完毕)
        String[] urls = getParaValues("urls");
        String[] types = getParaValues("types");
        String user_id = getUser().userId();
        String tribe_id = getPara(TRIBE_ID);
        String card_id = RandomUtils.randomCustomUUID();
        boolean saved = new Card()
                .set(Card.CARD_ID, card_id)
                .set(TRIBE_ID, tribe_id)
                .set(USER_ID, user_id)
                .set(DEVICE_NAME, device_name)
                .set(CARD_CONTENT, card_content)
                .set(CARD_DATE, DateUtils.currentTimeStamp())
                .set(CARD_ISEXIST, 1)
                .save();
        if (saved) {
            // 保存帖子成功后，保存帖子中媒体关联表
            if (urls != null) {
                int len = urls.length;
                if (types == null || types.length != len) {
                    renderFailed("urls must match with types");
                } else {
                    List<CardMedia> cardMedias = new ArrayList<>();
                    for (int i = 0; i < len; i++) {
                        CardMedia cardMedia = new CardMedia()
                                .set(MEDIA_ID, RandomUtils.randomCustomUUID())
                                .set(MEDIA_TYPE, types[i])
                                .set(MEDIA_URL, urls[i])
                                .set(CardMedia.CARD_ID, card_id);
                        cardMedias.add(cardMedia);
                    }
                    Db.batchSave(cardMedias, 100); //批量保存
                    renderSuccess("card save success");
                }
            }
        } else {
            // 删除上传文件
            if (urls != null) {
                for (String fileRelativePath : urls) {
                    FileUtils.delFileRelative(fileRelativePath);
                }
            }
            renderFailed("card save failed");
        }
    }
}

