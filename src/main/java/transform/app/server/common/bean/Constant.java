package transform.app.server.common.bean;


import transform.app.server.config.AppProperty;

/**
 * @author zhuqi259
 *         2016-03-23
 */
public class Constant {
    private static Constant me = new Constant();
    
    private String resourceServer;

    /**
     * 获取单例对象
     *
     * @return Constant
     */
    public static Constant me() {
        return me;
    }

    public String getResourceServer() {
        return AppProperty.me().resourcePrefix();
    }

    public void setResourceServer(String resourceServer) {
        this.resourceServer = resourceServer;
    }
}
