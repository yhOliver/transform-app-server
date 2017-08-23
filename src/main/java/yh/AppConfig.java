package yh;

import com.jfinal.config.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.render.ViewType;
import yh.config.Context;
import yh.handler.APINotFoundHandler;
import yh.handler.ContextHandler;
import yh.interceptor.ErrorInterceptor;
import yh.interceptor.LoginIntercepter;
import yh.model.User;
import yh.plugin.HikariCPPlugin;

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
        //me.add(new ActionRouter()); //页面路由
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

        // TODO 数据库表配置 [单元测试需相应添加]
        arp.addMapping("user", User.USER_ID, User.class);//用户表


    }

    /**
     * 拦截器配置
     */
    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new ErrorInterceptor());
        me.add(new LoginIntercepter());
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
