package transform.app.server;

import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.render.ViewType;
import transform.app.server.config.Context;
import transform.app.server.handler.APINotFoundHandler;
import transform.app.server.handler.ContextHandler;
import transform.app.server.interceptor.ErrorInterceptor;
import transform.app.server.model.*;
import transform.app.server.plugin.HikariCPPlugin;
import transform.app.server.router.APIRouter;

/**
 * JFinal总配置文件，挂接所有接口与插件
 *
 * @author zhuqi259
 */
public class AppConfig extends JFinalConfig {

    /**
     * 常量配置
     */
    @Override
    public void configConstant(Constants me) {
        //TODO 开发模式
        me.setDevMode(true);//开启开发模式
        me.setEncoding("UTF-8");
        me.setViewType(ViewType.JSP);
    }

    /**
     * 所有接口配置
     */
    @Override
    public void configRoute(Routes me) {
        // TODO 路由配置
        me.add(new APIRouter());//接口路由
        // me.add(new ActionRouter()); //页面路由
    }

    /**
     * 插件配置
     */
    @Override
    public void configPlugin(Plugins me) {

        //初始化连接池插件
        loadPropertyFile("jdbc.properties");
        HikariCPPlugin hcp = new HikariCPPlugin(getProperty("jdbcUrl"),
                getProperty("user"),
                getProperty("password"),
                getProperty("driverClass"),
                getPropertyToInt("maxPoolSize"));

        me.add(hcp);

        ActiveRecordPlugin arp = new ActiveRecordPlugin(hcp);
        me.add(arp);

        // TODO 数据库表配置
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
        arp.addMapping("tbpost_media", PostMedia.MEDIA_ID, PostMedia.class); //帖子-多媒体表（帖子中包含的图片、视频）
        arp.addMapping("tbpost_reply", PostReply.REPLY_ID, PostReply.class); //帖子回复表
        arp.addMapping("t_zan", Zan.class); //帖子赞表
    }

    /**
     * 拦截器配置
     */
    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new ErrorInterceptor());
    }

    /**
     * handle 配置*
     */
    @Override
    public void configHandler(Handlers me) {
        me.add(new ContextHandler());
        me.add(new APINotFoundHandler());
    }

    @Override
    public void afterJFinalStart() {
        Context.me().init();
    }

    @Override
    public void beforeJFinalStop() {
        Context.me().destroy();
    }
}
