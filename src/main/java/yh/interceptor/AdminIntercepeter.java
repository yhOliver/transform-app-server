package yh.interceptor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import yh.model.User;

/**
 * AdminIntercepeter
 *
 * @author <a href="mailto:acsbq_young@163.com">Yang Hang</a>
 * @version V1.0.0
 * @since 2017-08-11
 */
public class AdminIntercepeter implements Interceptor {


    @Override
    public void intercept(Invocation inv) {
        User user = User.dao.findFirst("select * from user where user_id = " + inv.getController().getSession().getAttribute("username"));
        if(user != null && user.getInt(User.type) ==  1){
            inv.invoke();
        }else{
            inv.getController().renderJson("非管理员用户禁止操作！");
        }
    }
}
