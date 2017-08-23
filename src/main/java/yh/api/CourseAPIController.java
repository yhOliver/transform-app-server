package yh.api;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.interceptor.LoginIntercepter;
import yh.model.Course;

import java.util.List;

/**
 * CourseAPIController
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-10
 */
@Before({POST.class, LoginIntercepter.class})
public class CourseAPIController extends BaseAPIController {

    private static final Logger logger = LoggerFactory.getLogger(CourseAPIController.class);

    public void list(){
        List<Course> courseList = Course.dao.find("select * from course");
        renderJson(new BaseResponse(Code.SUCCESS, "获取列表", courseList));
    }
}
