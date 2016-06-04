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
import transform.app.server.common.token.TokenManager;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.FileUtils;
import transform.app.server.common.utils.RandomUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.*;
import transform.app.server.model.Post;
import transform.app.server.model.PostReply;
import transform.app.server.model.User;
import transform.app.server.model.Zan;

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
 * <p>
 * 最新帖子暂时仅按照时间排序
 * <p>
 * 删帖:                           POST /api/post/del
 * 删回复:                         POST /api/post/delReply
 * 点赞人员列表:                    POST /api/post/zans
 *
 * @author zhuqi259
 */
@Before({POST.class, TokenInterceptor.class})
public class PostAPIController extends BaseAPIController {
    private static final String MEDIA_SPLIT = ",";
    private static final String CONTENT_SPLIT = "\\|";
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 发帖，用户状态数+1
     * <p>
     * 部落ID存在就是在部落内发帖，否则就是会员自己发的（tribe_id=null）,且均显示在个人动态和最新帖子中
     * <p>
     * POST、登陆状态、事务
     */
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
        // 修改 2016-05-29 by zhuqi259
        String urls = getPara("urls");
        //     String[] urls = getParaValues("urls");
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
                .set(NUM_OF_ZAN, 0)
                .set(Post.STATUS, 1);
        if (StringUtils.isNotEmpty(urls)) {
            //   String media_urls = StringUtils.join(urls);
            post.set(MEDIA_URLS, urls);
        } else if ("".equals(post_content)) {
            // urls == null && post_content为空，即帖子没有任何内容~~
            renderFailed("post must have something");
            return;
        }
        boolean saved = post.save();
        if (saved) {
            // 发帖成功，用户状态数+1
            Db.update("UPDATE tbuser SET num_of_status = num_of_status+1 WHERE user_id = ?", user_id);
            renderSuccess("post save success");
        } else {
            // 删除上传文件
            if (StringUtils.isNotEmpty(urls)) {
                // modified by zhuqi259 @ 2016-05-29
                //  0|path1|path2;0|path3|path4;1|path5|path6;0|path7|path8
                String[] customFiles = urls.split(MEDIA_SPLIT);
                for (String customFile : customFiles) {
                    String[] data = customFile.split(CONTENT_SPLIT);
                    if (data.length < 3) {  // 0|path1|path2
                        renderFailed("post save failed");
                        return;
                    } else {
                        FileUtils.delFileRelative(data[1]);
                        FileUtils.delFileRelative(data[2]);
                    }
                }
            }
            renderFailed("post save failed");
        }
    }

    /**
     * 回复帖子，帖子的评论数更新
     * <p>
     * 用户能看到这个帖子就说明其可以评论和点赞（不能的就查不出来。。。不是部落中的或者非关注对象发的帖子）
     * 故这里不再检查权限了，TODO 有待进一步明确
     * <p>
     * POST、登陆状态、检查帖子存在、事务
     */
    @Before({PostStatusInterceptor.class, Tx.class})
    public void reply() {
        String post_id = getPara(PostReply.POST_ID);
        String reply_content = getPara(REPLY_CONTENT);
        //校验必填项参数
        if (!notNull(Require.me()
                .put(reply_content, "reply content can not be null"))) {
            return;
        }
        // 被回复者ID
        String reply_to_user_id = getPara(REPLY_TO_USER_ID);
        if (StringUtils.isNotEmpty(reply_to_user_id)) {
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
                .set(PostReply.STATUS, 1)
                .save();
        if (saved) {
            // 评论成功，评论数+1
            Db.update("UPDATE tbpost SET num_of_reply = num_of_reply+1 WHERE post_id = ?", post_id);
        }
        renderJson(new BaseResponse(saved ? Code.SUCCESS : Code.FAILURE, saved ? "reply save success" : "reply save failed"));
    }

    /**
     * 所有用户都可以看到，无需登录
     * <p>
     * POST、检查帖子存在
     */
    @Clear
    @Before({POST.class, PostStatusInterceptor.class})
    public void replies() {
        // 查找该贴子的回复列表
        String post_id = getPara(PostReply.POST_ID);
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        // 此时帖子已存在，只需判断回复状态
        /**
         SELECT tpr.*, tu.user_nickname, tu.user_photo, tu2.user_nickname AS reply_to_username
         FROM (SELECT * FROM tbpost_reply WHERE post_id = ? AND status=1) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id LEFT JOIN tbuser tu2 ON tpr.reply_to_user_id = tu2.user_id ORDER BY tpr.reply_date
         */
        Page<Record> post_replies = Db.paginate(pageNumber, pageSize, "SELECT tpr.*, tu.user_nickname, tu.user_photo, tu2.user_nickname AS reply_to_username",
                "FROM (SELECT * FROM tbpost_reply WHERE post_id = ? AND status=1) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id LEFT JOIN tbuser tu2 ON tpr.reply_to_user_id = tu2.user_id ORDER BY tpr.reply_date", post_id); // LEFT JOIN 没问题
        renderJson(new BaseResponse(Code.SUCCESS, "", post_replies));
    }

    /**
     * 点赞或取消赞，帖子的赞数更新 => 与评论类似，能看到就可以赞
     * 故这里不再检查权限了，TODO 有待进一步明确
     * <p>
     * POST、登陆状态、检查帖子存在、事务
     */
    @Before({PostStatusInterceptor.class, Tx.class})
    public void zan() {
        int zan_flag = getParaToInt("zan_flag", 1); // 1点赞、0取消赞
        String user_id = getUser().userId();
        String post_id = getPara(Post.POST_ID);
        if (zan_flag == 1) { // 点赞
            // 查看是否已赞？
            if (Db.findFirst("SELECT * FROM t_zan WHERE post_id=? AND user_id=?", post_id, user_id) == null) {
                boolean saved = new Zan()
                        .set(Zan.ID, RandomUtils.randomCustomUUID())
                        .set(Zan.POST_ID, post_id)
                        .set(Zan.USER_ID, user_id)
                        .set(Zan.OCCURRENCE_TIME, DateUtils.currentTimeStamp())
                        .save();
                if (saved) {
                    // 赞+1
                    Db.update("UPDATE tbpost SET num_of_zan = num_of_zan+1 WHERE post_id = ?", post_id);
                    renderSuccess("zan success");
                } else {
                    renderFailed("zan failed");
                }
            } else {
                renderFailed("you have already zan this post");
            }
        } else { // 取消赞
            // 查看是否已赞？
            if (Db.findFirst("SELECT * FROM t_zan WHERE post_id=? AND user_id=?", post_id, user_id) != null) {
                // 删除 赞表记录
                int deleted = Db.update("DELETE FROM t_zan WHERE post_id=? AND user_id=?", post_id, user_id);
                if (deleted > 0) {
                    // 赞-1
                    Db.update("UPDATE tbpost SET num_of_zan = num_of_zan-1 WHERE post_id = ?", post_id);
                    renderSuccess("unzan success");
                } else {
                    renderFailed("unzan failed");
                }
            } else {
                renderFailed("you have not zan this post, no need to unzan");
            }
        }
    }

    /**
     * 查看帖子详情
     * <p>
     * POST、检查帖子存在
     */
    @Clear
    @Before({POST.class, PostStatusInterceptor.class})
    public void detail() {
        String post_id = getPara(Post.POST_ID);
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageSize < 1) {
            renderFailed("pageSize must more than 0");
            return;
        }
        Record post;
        // 登陆状态 与 非登陆状态
        String token = getPara("token");
        if (StringUtils.isNotEmpty(token)) {
            // 登陆状态
            User user = TokenManager.getMe().validate(token);
            if (user == null) {
                renderFailed("token is invalid");
                return;
            }
            String user_id = user.userId();
            /**
             * SELECT tp.*, tt.tribe_name, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status FROM (SELECT * FROM tbpost WHERE post_id = ? AND status = 1) tp LEFT JOIN tbtribe tt ON tp.tribe_id = tt.tribe_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id
             */
            post = Db.findFirst("SELECT tp.*, tt.tribe_name, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status FROM (SELECT * FROM tbpost WHERE post_id = ? AND status = 1) tp LEFT JOIN tbtribe tt ON tp.tribe_id = tt.tribe_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id", post_id, user_id);
        } else {
            /**
             * SELECT tp.*, tt.tribe_name, 0 AS zan_status FROM (SELECT * FROM tbpost WHERE post_id = ? AND status = 1) tp LEFT JOIN tbtribe tt ON tp.tribe_id = tt.tribe_id
             */
            post = Db.findFirst("SELECT tp.*, tt.tribe_name, 0 AS zan_status FROM (SELECT * FROM tbpost WHERE post_id = ? AND status = 1) tp LEFT JOIN tbtribe tt ON tp.tribe_id = tt.tribe_id", post_id);
        }
        // 赞 (前10个赞)
        Page<Record> zans = Db.paginate(1, 10, "SELECT tu.user_id, tu.user_photo", "FROM (SELECT user_id FROM t_zan WHERE post_id = ? ORDER BY occurrence_time DESC) tz LEFT JOIN tbuser tu ON tz.user_id = tu.user_id", post_id); // LEFT JOIN 没问题
        // 评论第一页
        /**
         SELECT tpr.*, tu.user_nickname, tu.user_photo, tu2.user_nickname AS reply_to_username
         FROM (SELECT * FROM tbpost_reply WHERE post_id = ? AND status=1) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id LEFT JOIN tbuser tu2 ON tpr.reply_to_user_id = tu2.user_id ORDER BY tpr.reply_date
         */
        Page<Record> post_replies = Db.paginate(1, pageSize, "SELECT tpr.*, tu.user_nickname, tu.user_photo, tu2.user_nickname AS reply_to_username",
                "FROM (SELECT * FROM tbpost_reply WHERE post_id = ? AND status=1) tpr LEFT JOIN tbuser tu ON tpr.user_id = tu.user_id LEFT JOIN tbuser tu2 ON tpr.reply_to_user_id = tu2.user_id ORDER BY tpr.reply_date", post_id); // LEFT JOIN 没问题

        PostDetailVO vo = new PostDetailVO();
        vo.setPost(post);
        vo.setZans(zans);
        vo.setReplies(post_replies);
        renderJson(new BaseResponse(Code.SUCCESS, "", vo));
    }

    /**
     * 点赞人员列表
     * <p>
     * POST、检查帖子存在
     */
    @Clear
    @Before({POST.class, PostStatusInterceptor.class})
    public void zans() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber);//页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String post_id = getPara(Post.POST_ID);
        Page<Record> rs = Db.paginate(pageNumber, pageSize, "SELECT tu.user_id, tu.user_nickname, tu.user_photo", "FROM (SELECT user_id FROM t_zan WHERE post_id = ? ORDER BY occurrence_time DESC) tz LEFT JOIN tbuser tu ON tz.user_id = tu.user_id", post_id); // LEFT JOIN 没问题
        renderJson(new BaseResponse(Code.SUCCESS, "", rs));
    }


    /**
     * 部落内帖子列表, 所有人可见，无需登录
     * <p>
     * POST、检查部落存在
     */
    @Clear
    @Before({POST.class, TribeStatusInterceptor.class})
    public void thread() {
        // 发帖人，头像，时间，设备，发帖内容，媒体，评论数、赞数
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String tribe_id = getPara(TRIBE_ID);
        // 登陆状态 与 非登陆状态
        String token = getPara("token");
        if (StringUtils.isNotEmpty(token)) {
            // 登陆状态
            User user = TokenManager.getMe().validate(token);
            if (user == null) {
                renderFailed("token is invalid");
                return;
            }
            String user_id = user.userId();
            /**
             SELECT tp.*, tu.user_nickname, tu.user_photo, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status
             FROM (SELECT * FROM tbpost WHERE tribe_id = ? AND status = 1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id ORDER BY post_date DESC
             */
            Page<Record> thread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status",
                    "FROM (SELECT * FROM tbpost WHERE tribe_id = ? AND status = 1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id ORDER BY post_date DESC", tribe_id, user_id); // LEFT JOIN 没问题
            renderJson(new BaseResponse(Code.SUCCESS, "", thread));
        } else {
            /**
             SELECT tp.*, tu.user_nickname, tu.user_photo, 0 AS zan_status
             FROM (SELECT * FROM tbpost WHERE tribe_id = ? AND status=1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC
             */
            Page<Record> thread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo, 0 AS zan_status",
                    "FROM (SELECT * FROM tbpost WHERE tribe_id = ? AND status=1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC", tribe_id); // LEFT JOIN 没问题
            renderJson(new BaseResponse(Code.SUCCESS, "", thread));
        }
    }

    /**
     * 最新帖子, 是个人就可以看到，都不需要登陆
     * <p>
     * POST
     */
    @Clear
    @Before(POST.class)
    public void latest() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        // 登陆状态 与 非登陆状态
        String token = getPara("token");
        if (StringUtils.isNotEmpty(token)) {
            // 登陆状态
            User user = TokenManager.getMe().validate(token);
            if (user == null) {
                renderFailed("token is invalid");
                return;
            }
            String user_id = user.userId();
            /**
             SELECT tp.*, tu.user_nickname, tu.user_photo, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status
             FROM (SELECT * FROM tbpost WHERE status = 1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id ORDER BY post_date DESC
             */
            Page<Record> latestThread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo, (CASE WHEN tz.post_id IS NULL THEN 0 ELSE 1 END) AS zan_status",
                    "FROM (SELECT * FROM tbpost WHERE status = 1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id LEFT JOIN (SELECT post_id, user_id FROM t_zan WHERE user_id = ?) tz ON tp.post_id = tz.post_id ORDER BY post_date DESC", user_id); // LEFT JOIN 没问题
            renderJson(new BaseResponse(Code.SUCCESS, "", latestThread));
        } else {
            /**
             SELECT tp.*, tu.user_nickname, tu.user_photo, 0 AS zan_status
             FROM (SELECT * FROM tbpost WHERE status = 1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC
             */
            Page<Record> latestThread = Db.paginate(pageNumber, pageSize, "SELECT tp.*, tu.user_nickname, tu.user_photo, 0 AS zan_status",
                    "FROM (SELECT * FROM tbpost WHERE status=1) tp LEFT JOIN tbuser tu ON tp.user_id = tu.user_id ORDER BY post_date DESC"); // LEFT JOIN 没问题
            renderJson(new BaseResponse(Code.SUCCESS, "", latestThread));
        }
    }

    /**
     * 删帖， 帖子发布者 （用户状态数-1）
     * <p>
     * POST、登陆、帖子状态、事务
     */
    @Before({PostStatusInterceptor.class, Tx.class})
    public void del() {
        String user_id = getUser().userId();
        String post_id = getPara(Post.POST_ID);
        // 删除帖子
        int deleted = Db.update("UPDATE tbpost SET status=0 WHERE post_id=? AND user_id=?", post_id, user_id); // 只能删自己的帖子
        if (deleted > 0) {
            // 删帖成功 => 用户状态数-1
            Db.update("UPDATE tbuser SET num_of_status = num_of_status-1 WHERE user_id = ?", user_id);
            renderSuccess("del post success");
        } else {
            renderFailed("del post failed");
        }
    }

    /**
     * 删回复（帖子的回复数-1）
     * <p>
     * POST、登陆、回复者、事务
     */
    @Before(Tx.class)
    public void delReply() {
        String user_id = getUser().userId();
        String reply_id = getPara(PostReply.REPLY_ID);
        if (StringUtils.isEmpty(reply_id)) {
            renderFailed("reply id can not be null");
            return;
        }
        PostReply postReply = PostReply.dao.findFirst("SELECT * FROM tbpost_reply WHERE reply_id=? AND status=1", reply_id);
        if (postReply == null) {
            renderFailed("reply is not found");
            return;
        }
        // 删除回复
        int deleted = Db.update("UPDATE tbpost_reply SET status=0 WHERE reply_id=? AND user_id=?", reply_id, user_id); // 只能删自己的回复
        if (deleted > 0) {
            // 删回复成功，帖子的回复数-1
            String post_id = postReply.getStr(PostReply.POST_ID);
            Db.update("UPDATE tbpost SET num_of_reply = num_of_reply-1 WHERE post_id = ?", post_id);
            renderSuccess("del reply success");
        } else {
            renderFailed("del reply failed");
        }
    }
}

