package transform.app.server.common.bean;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import transform.app.server.model.Post;

/**
 * 帖子详情表
 */
public class PostDetailVO {
    private Record post; //  帖子基本信息
    private Page<?> zans; // 赞
    private Page<?> replies; // 回复

    public Record getPost() {
        return post;
    }

    public void setPost(Record post) {
        this.post = post;
    }

    public Page<?> getZans() {
        return zans;
    }

    public void setZans(Page<?> zans) {
        this.zans = zans;
    }

    public Page<?> getReplies() {
        return replies;
    }

    public void setReplies(Page<?> replies) {
        this.replies = replies;
    }
}
