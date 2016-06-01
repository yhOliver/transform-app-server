package transform.app.server.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import org.junit.After;
import org.junit.BeforeClass;
import transform.app.server.plugin.HikariCPPlugin;

/**
 * @author zhuqi259
 *         JFinal的Model测试用例
 */
public class JFinalModelCase {

    protected static HikariCPPlugin hcp;
    protected static ActiveRecordPlugin arp;

    /**
     * 数据连接地址
     */
    private static final String URL = "jdbc:mysql://192.168.238.149:3306/tuibian";

    /**
     * 数据库账号
     */
    private static final String USERNAME = "tuibian";

    /**
     * 数据库密码
     */
    private static final String PASSWORD = "root";

    /**
     * 数据库驱动
     */
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        hcp = new HikariCPPlugin(URL,
                USERNAME, PASSWORD, DRIVER, 10);
        hcp.start();

        arp = new ActiveRecordPlugin(hcp);

        // TODO 单元测试添加数据库表
        arp.addMapping("tbuser", User.USER_ID, User.class);//用户表
        arp.addMapping("t_register_code", RegisterCode.MOBILE, RegisterCode.class); //注册验证码对象
        arp.addMapping("t_feedback", FeedBack.ID, FeedBack.class); //意见反馈表
        arp.addMapping("tbvenue", Venue.VENU_ID, Venue.class); //场馆表
        arp.addMapping("tbsport_typedic", SportType.SPTY_ID, SportType.class); //运动类别表
        arp.addMapping("tbvenue_sport", VenueSport.VESP_ID, VenueSport.class); //场馆-运动类别关联表
        arp.addMapping("tbvenue_comment", VenueComment.VECO_ID, VenueComment.class); //场馆评价表
        arp.addMapping("t_distance", Distance.class); //设备-场馆距离表
        arp.addMapping("tbtribe", Tribe.TRIBE_ID, Tribe.class); //部落表
        arp.addMapping("tbtribe_member", TribeMember.ID, TribeMember.class); //部落成员表
        arp.addMapping("tbpost", Post.POST_ID, Post.class); //帖子表
        arp.addMapping("tbpost_reply", PostReply.REPLY_ID, PostReply.class); //帖子回复表
        arp.addMapping("t_zan", Zan.class); //帖子赞表
        arp.addMapping("tbuser_concern", UserConcern.ID, UserConcern.class);//用户-关注表
        arp.addMapping("tbgoods_catagory", GoodsCategory.CATA_ID, GoodsCategory.class); // 商品类别表
        arp.addMapping("tbconsignee_address", ConsigneeAddress.CONSIGNEE_ID, ConsigneeAddress.class); // 收货地址表
        arp.addMapping("tbgoods", Goods.GOODS_ID, Goods.class); //商品表

        arp.start();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        arp.stop();
        hcp.stop();
    }
}
