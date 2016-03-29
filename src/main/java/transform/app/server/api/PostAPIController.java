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
import transform.app.server.common.bean.PostDetailVO;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.FileUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.*;
import transform.app.server.model.Post;
import transform.app.server.model.PostReply;
import transform.app.server.model.Zan;

import java.util.List;

import static transform.app.server.model.Post.*;
import static transform.app.server.model.PostReply.*;


/**
 * 帖子相关的接口*
 * <p>
 * 发帖:                           POST /api/post/add
 * 回复:                           POST /api/post/reply
 * 查看部落内帖子列表（分页）:     POST /api/post/thread
 * 帖子详情:                       POST /api/post/detail
 * 帖子回复更多分页:               POST /api/post/replies
 * 点赞:                           POST /api/post/zan
 * 最新帖子:                       POST /api/post/latest
 * 某个用户的帖子列表:             POST /api/post/postsOfSomeOne (就是用户动态)
 * <p>
 * 部落内帖子列表, 所有会员可见
 * <p>
 * 非部落成员可以点赞与查看评论，部落成员可以发帖、评论
 * <p>
 * 最新帖子暂时仅按照时间排序
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class PostAPIController extends BaseAPIController {
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 发帖
     */
    @Before({TribeMemberInterceptor.class, Tx.class})
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
        String user_id = getUser().userId();
        String tribe_id = getPara(TRIBE_ID);
        String post_id = RandomUtils.randomCustomUUID();
        Post post = new Post()
                .set(Post.POST_ID, post_id)
                .set(Post.TRIBE_ID, tribe_id)
                .set(Post.USER_ID, user_id)
                .set(DEVICE_NAME, device_name)
                .set(POST_CONTENT, post_content)
                .set(POST_DATE, DateUtils.currentTimeStamp())
                .set(NUM_OF_REPLY, 0)
                .set(NUM_OF_ZAN, 0);
        if (urls != null) {
            String media_urls = StringUtils.join(urls);
            post.set(MEDIA_URLS, media_urls);
        } else if ("".equals(post_content)) {
            // 帖子没有内容~~
            renderFailed("post must have something");
            return;
        }
        boolean saved = post.save();
        if (saved) {
            renderSuccess("post save success");
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

    /**
     * 回复帖子
     */
    @Before({TribeMemberInterceptor.class, PostReplyInterceptor.class, Tx.class})
    public void reply() {
        String post_id = getPara(PostReply.POST_ID);
        String reply_content = getPara(REPLY_CONTENT);
        //校验必填项参数
        if (!notNull(Require.me()
                .put(reply_content, "reply content can not be null"))) {
            return;
        }
        // 被回复者ID (0 表示回复帖子)
        String reply_to_user_id = getPara(REPLY_TO_USER_ID, "0");
        if (!"0".equals(reply_to_user_id)) {
            // 查找该贴子的回复者
            Record reply_to_user = Db.findFirst("SELECT * FROM tbpost_reply WHERE post_id = ? AND user_id = ?", post_id, reply_to_user_id);
            if (reply_to_user == null) {
                renderFailed("user reply to is not existed in this post");
                return;
            }
        }
        String user_id = getUser().userId();
        boolean saved = new PostReply()
                .set(PostReply.REPLY_ID, RandomUtils.randomCustomUUID())
                .set(PostReply.POST_ID, post_id)
                .set(PostReply.USER_ID, user_id)
                .set(REPLY_TO_USER_ID, reply_to_user_id)
                .set(REPLY_CONTENT, reply_content)
                .set(REPLY_DATE, DateUtils.currentTimeStamp())
                .save();
        renderJson(new BaseResponse(saved ? Code.SUCCESS : Code.FAILURE, saved ? "reply save success" : "reply save failed"));
    }

    @Before({TribeStatusInterceptor.class, PostReplyInterceptor.class})
    public void replies() {
        // 查找该贴子的回复列表
        String post_id = getPara(PostReply.POST_ID);
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        /**
         SELECT tpr.*, tu.user_nickname, tu.user_photo
         FROM ( SELECT * FROM tbpost_reply WHERE post_id = ? ) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id ORDER BY tpr.reply_date
         */
        Page<Record> post_replies = Db.paginate(pageNumber, pageSize, "SELECT tpr.*, tu.user_nickname, tu.user_photo",
                "FROM ( SELECT * FROM tbpost_reply WHERE post_id = ? ) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id ORDER BY tpr.reply_date", post_id);
        renderJson(new BaseResponse(post_replies));
    }

    /**
     * 赞
     */
    @Before({TribeStatusInterceptor.class, PostReplyInterceptor.class})
    public void zan() {
        int zan_flag = getParaToInt("zan_flag", 1); // 1点赞、0取消赞
        String user_id = getUser().userId();
        String post_id = getPara(Post.POST_ID);
        // 删除 赞表记录
        Db.update("DELETE FROM t_zan WHERE post_id=? AND user_id=?", post_id, user_id);
        if (zan_flag == 1) {
            new Zan().set(Zan.POST_ID, post_id).set(Zan.USER_ID, user_id).save();
        }
        renderSuccess("success");
    }

    /**
     * 查看帖子详情
     */
    @Before({TribeStatusInterceptor.class, PostReplyInterceptor.class})
    public void detail() {
        String post_id = getPara(Post.POST_ID);
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        Post post = getAttr("post");
        // 赞
        List<Record> zans = Db.find("SELECT tu.user_id, tu.user_photo FROM (SELECT user_id FROM t_zan WHERE post_id = ?) tz LEFT JOIN tbuser tu ON tz.user_id = tu.user_id", post_id);
        // 评论第一页
        Page<Record> post_replies = Db.paginate(1, pageSize, "SELECT tpr.*, tu.user_nickname, tu.user_photo",
                "FROM ( SELECT * FROM tbpost_reply WHERE post_id = ? ) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id ORDER BY tpr.reply_date", post_id);
        PostDetailVO vo = new PostDetailVO();
        vo.setPost(post);
        vo.setZans(zans);
        vo.setReplies(post_replies);
        renderJson(new BaseResponse(vo));
    }

    /**
     * 部落内帖子列表, 所有会员可见
     */
    @Before(TribeStatusInterceptor.class)
    public void thread() {
        // 发帖人，头像，时间，设备，发帖内容，媒体，评论数、赞数
        /**
         SELECT tp.*, tu.user_nickname, tu.user_photo
         FROM (SELECT * FROM tbpost WHERE tribe_id = ?) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC
         */
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        String tribe_id = getPara(TRIBE_ID);
        Page<Record> thread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo",
                "FROM (SELECT * FROM tbpost WHERE tribe_id = ?) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC", tribe_id);
        renderJson(new BaseResponse(thread));
    }

    /**
     * 最新帖子, 是个人就可以看到，都不需要登陆
     */
    @Clear
    @Before(POST.class)
    public void latest() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        Page<Record> latestThread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo",
                "FROM tbpost tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC");
        renderJson(new BaseResponse(latestThread));
    }


    /**
     * 某个用户的帖子列表，是个人就可以看到，都不需要登陆，(就是用户动态)
     */
    @Clear
    @Before(POST.class)
    public void postsOfSomeOne() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        String user_id = getPara(Post.USER_ID);
        if (StringUtils.isEmpty(user_id)) {
            renderFailed("user id can not be null");
            return;
        }
        Page<Record> latestThread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo",
                "FROM (SELECT * FROM tbpost WHERE user_id = ?) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC", user_id);
        renderJson(new BaseResponse(latestThread));
    }
}

