package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import transform.app.server.common.Require;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.FileUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.interceptor.TribeMemberInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.TokenInterceptor;
import transform.app.server.model.Post;
import transform.app.server.model.PostMedia;

import java.util.ArrayList;
import java.util.List;

import static transform.app.server.model.Post.*;
import static transform.app.server.model.PostMedia.*;


/**
 * 帖子相关的接口*
 * <p>
 * 发帖:                           POST /api/post/add
 * 回复:                           POST /api/post/reply
 * 查看部落内帖子列表（分页）:     POST /api/post/thread
 * 帖子详情:                       POST /api/post/detail
 * 帖子回复更多分页:               POST /api/post/replies
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class, TribeMemberInterceptor.class})
public class PostAPIController extends BaseAPIController {
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
        String post_content = getPara(POST_CONTENT, "");
        // 上传文件，调用文件上传接口 (已经上传完毕)
        String[] urls = getParaValues("urls");
        String[] types = getParaValues("types");
        String user_id = getUser().userId();
        String tribe_id = getPara(TRIBE_ID);
        String post_id = RandomUtils.randomCustomUUID();
        boolean saved = new Post()
                .set(Post.POST_ID, post_id)
                .set(TRIBE_ID, tribe_id)
                .set(USER_ID, user_id)
                .set(DEVICE_NAME, device_name)
                .set(POST_CONTENT, post_content)
                .set(POST_DATE, DateUtils.currentTimeStamp())
                .set(POST_ISEXIST, 1)
                .save();
        if (saved) {
            // 保存帖子成功后，保存帖子中媒体关联表
            if (urls != null) {
                int len = urls.length;
                if (types == null || types.length != len) {
                    renderFailed("urls must match with types");
                } else {
                    List<PostMedia> postMedias = new ArrayList<>();
                    for (int i = 0; i < len; i++) {
                        PostMedia postMedia = new PostMedia()
                                .set(MEDIA_ID, RandomUtils.randomCustomUUID())
                                .set(MEDIA_TYPE, types[i])
                                .set(MEDIA_URL, urls[i])
                                .set(PostMedia.POST_ID, post_id);
                        postMedias.add(postMedia);
                    }
                    Db.batchSave(postMedias, 100); //批量保存
                    renderSuccess("post save success");
                }
            }
        } else {
            // 删除上传文件
            if (urls != null) {
                for (String fileRelativePath : urls) {
                    FileUtils.delFileRelative(fileRelativePath);
                }
            }
            renderFailed("post save failed");
        }
    }
}

