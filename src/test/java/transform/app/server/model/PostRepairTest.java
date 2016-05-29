package transform.app.server.model;

import org.junit.Test;
import transform.app.server.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static transform.app.server.model.Post.MEDIA_URLS;

/**
 * 修复帖子数据【仅使用一次】
 */
public class PostRepairTest extends JFinalModelCase {

    private Post dao = Post.dao;

    @Test
    public void testFindAll() {
        List<Post> posts = dao.find("SELECT * FROM tbpost");
        for (Post post : posts) {
            System.out.println(post);
        }
    }

    /**
     * 修复帖子数据 [媒体数据]
     * media_urls
     * 原格式 xxx;xxx;xxx
     * 现格式 x|x|x;x|x|x;x|x|x
     */
    @Test
    public void testRepairPostData() {
        List<Post> posts = dao.find("SELECT * FROM tbpost");
        for (Post post : posts) {
            System.out.println(post);
            String urls = post.getStr(MEDIA_URLS);
            if (urls != null) {
                // 先删除所有带视频的帖子
                if (urls.contains(".mp4") || urls.contains(".3gp")) {
                    post.delete();
                } else {
                    // 原格式 xxx;xxx;xxx
                    String[] customFiles = urls.split(",");
                    // 现格式 x|x|x;x|x|x;x|x|x
                    List<String> result = new ArrayList<>();
                    for (String customFile : customFiles) {
                        String one = "0|" + customFile + "|" + customFile;
                        result.add(one);
                    }
                    post.set(MEDIA_URLS, StringUtils.join(result.toArray()));
                    post.update();
                }
            }
        }
    }
}
