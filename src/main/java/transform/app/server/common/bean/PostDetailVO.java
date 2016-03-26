package transform.app.server.common.bean;

import com.jfinal.plugin.activerecord.Page;
import transform.app.server.model.Post;

import java.util.List;

/**
 * 帖子详情表
 */
public class PostDetailVO {
    private Post post; //  帖子基本信息
    private List<?> media; // 帖子的图片或视频
    private List<?> zans; // 赞
    private Page<?> replies; // 回复

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<?> getMedia() {
        return media;
    }

    public void setMedia(List<?> media) {
        this.media = media;
    }

    public List<?> getZans() {
        return zans;
    }

    public void setZans(List<?> zans) {
        this.zans = zans;
    }

    public Page<?> getReplies() {
        return replies;
    }

    public void setReplies(Page<?> replies) {
        this.replies = replies;
    }
}
