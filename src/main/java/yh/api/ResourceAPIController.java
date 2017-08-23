package yh.api;

import com.jfinal.aop.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.interceptor.LoginIntercepter;
import yh.interceptor.POST;
import yh.model.Course;

import java.util.List;

/**
 * ResourceAPIController
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-10
 */
@Before({POST.class, LoginIntercepter.class})
public class ResourceAPIController extends BaseAPIController{

    private static final Logger logger = LoggerFactory.getLogger(ResourceAPIController.class);

    public void list(){
        List<Course> courseList = Course.dao.find("select * from course");
        renderJson(new BaseResponse(Code.SUCCESS, "获取资源列表成功", courseList));
    }
}
