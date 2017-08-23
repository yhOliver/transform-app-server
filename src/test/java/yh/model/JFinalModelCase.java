package yh.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import org.junit.After;
import org.junit.BeforeClass;
import yh.plugin.HikariCPPlugin;

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
        arp.addMapping("user", User.USER_ID, User.class);//用户表
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
