package yh.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Course
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-10
 */
public class Course extends Model<Course>{

    public static String COURSE_ID = "course_id";
    public static String COURSE_NAME = "course_name";
    public static String COURSE_DESC = "course_desc";

    public static final Course dao = new Course();

}
