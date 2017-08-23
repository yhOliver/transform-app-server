package yh.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import yh.interceptor.LoginIntercepter;
import yh.interceptor.POST;

/**
 * Resource
 *
 * "id":"1",
  "title":"机器人操作",
  "desc":"机器人操作巴拉巴拉巴拉巴巴拉巴拉巴拉",
  "target":"/jsp/courses/demo.html",
  "type":"1"
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-10
 */
@Before({POST.class, LoginIntercepter.class})
public class Resource extends Model<Resource>{

    public static String id = "id";
    public static String title = "name";
    public static String desc = "desc";
    public static String target = "target";
    public static String type = "type";

}
