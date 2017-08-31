package yh.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yh.common.Require;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.common.utils.DateUtils;
import yh.interceptor.AdminIntercepeter;
import yh.interceptor.POST;
import yh.model.Favorite;
import yh.model.User;
import yh.model.Watch;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;


@Before({POST.class})
public class AccountAPIController extends BaseAPIController {

    private static final Logger logger = LoggerFactory.getLogger(AccountAPIController.class);

    /**
     * 用户注册
     * <p>
     * 无登陆约束，只需POST，事务
     */
    @Clear
    @Before({POST.class, Tx.class})
    public void register() {
        //必填信息
        String user_mobile = getPara(User.USER_MOBILE);//手机号
        String user_code = getPara(User.USER_CODE);   //学号
        String nickname = getPara(User.USER_NICKNAME);//昵称
        String password = getPara(User.USER_PASSWORD);//密码 (已经加密过了)

        //校验必填项参数
        if (!notNull(Require.me()
                .put(user_code, "学号不能为空")
                .put(user_mobile, "手机号不能为空")
                .put(nickname, "昵称不能为空")
                .put(password, "密码不能为空"))) {
            return;
        }

        //检查账户是否已被注册
        if (Db.findFirst("SELECT * FROM user WHERE user_code = ?", user_code) != null) {
            renderJson(new BaseResponse(Code.FAILURE, "学号已经注册"));
            return;
        }

        //保存用户数据
        new User()
                .set(User.USER_MOBILE, user_mobile)
                .set(User.USER_PASSWORD, password)
                .set(User.USER_NICKNAME, nickname)
                .set(User.CREATETIME, DateUtils.currentTimeStamp())
                .set(User.USER_CODE, user_code)
                .set(User.STATUS, 0)
                .set(User.type, 0)
                .save();

        renderJson(new BaseResponse(Code.SUCCESS, "注册成功,返回登录界面"));
    }




    /**
     * 登录接口
     * <p>
     * 无登陆约束，只需POST
     */
    @Clear
    @Before(POST.class)
    public void login() {
        String user_code = getPara(User.USER_CODE);
        String password = getPara(User.USER_PASSWORD);
        //校验参数, 确保不能为空
        if (!notNull(Require.me()
                .put(user_code, "学号不能为空")
                .put(password, "密码不能为空")
        )) {
            return;
        }
        String sql = "SELECT * FROM user WHERE user_code = ? AND user_password = ?";
        User nowUser = User.dao.findFirst(sql, user_code, password);
        if (nowUser == null) {
            renderJson(new BaseResponse(Code.FAILURE, "学号或密码错误"));
            return;
        }else if(nowUser.getInt(User.STATUS) == 0){
            renderJson(new BaseResponse(Code.FAILURE, "请联系管理员通过授权"));
            return;
        }else{
            getSession().setAttribute("username", nowUser.userId());
            nowUser.remove(User.STATUS, User.USER_MOBILE, User.USER_ID, User.USER_PASSWORD, User.USER_CODE);
            Cookie cookie = new Cookie("username", user_code);
            cookie.setMaxAge(648000000);
            cookie.setPath("/");
            Cookie cookie1;
            try {
                String username = URLDecoder.decode(nowUser.getStr("user_nickname"),"utf-8");
                cookie1 = new Cookie("usernickname", username);
            } catch (UnsupportedEncodingException e) {
                cookie1 = new Cookie("usernickname", "username");
                e.printStackTrace();
            }

            cookie1.setMaxAge(648000000);
            cookie1.setPath("/");
            Cookie cookie2 = new Cookie("user_type", String.valueOf(nowUser.getInt(User.type)));
            cookie2.setMaxAge(648000000);
            cookie2.setPath("/");
            getResponse().addCookie(cookie);
            getResponse().addCookie(cookie1);
            getResponse().addCookie(cookie2);
            renderJson(new BaseResponse(Code.SUCCESS, "登录成功,跳转到首页", nowUser));
        }
    }

    @Before({AdminIntercepeter.class, Tx.class})
    public void auth(){
        String ids = getPara("ids");
        int type = getParaToInt("type");
        if (!notNull(Require.me().put(ids, "ids不能为空").put(type, "type不能为空"))) return;

        String[] idsArray = ids.split("-");
        int size = idsArray.length;
        List<String> sqls = new LinkedList<>();

        if (type == 0){
            for (int i = 0; i < size; i++){
                String sql = "delete from user where user_id = " + idsArray[i];
                sqls.add(sql);
            }
        }else if(type == 1){
            for (int i = 0; i < size; i++){
                String sql = "update user set status = "+ type +" where user_id = " + idsArray[i];
                sqls.add(sql);
            }
        }else {
            renderJson(new BaseResponse(Code.FAILURE, "type不正确"));
            return;
        }

        try {
            Db.batch(sqls, size);
            renderJson(new BaseResponse(Code.SUCCESS, "审核成功"));
        }catch (Exception e){
            renderJson(new BaseResponse(Code.FAILURE, "审核失败！"));
            e.printStackTrace();
        }


    }

    @Clear(POST.class)
    @Before({AdminIntercepeter.class})
    public void authList(){
        List<User> users =  User.dao.find("select * from user where status = ?", 0);
        for (User user : users){
            user.remove(User.STATUS, User.USER_PASSWORD);
        }
        renderJson(new BaseResponse(Code.SUCCESS, "返回列表成功", users));
    }

    @Clear(POST.class)
    public void allusers(){
        List<User> users =  User.dao.find("select * from user where status = ?", 1);
        for (User user : users){
            user.remove(User.STATUS, User.USER_PASSWORD);
        }
        renderJson(new BaseResponse(Code.SUCCESS, "返回列表成功", users));
    }

    @Clear(POST.class)
    public void logout(){
        getSession().removeAttribute("username");
        Cookie[] cookies = getRequest().getCookies();

        for(Cookie cookie : cookies){
            cookie.setMaxAge(0);
            cookie.setPath("/");
            getResponse().addCookie(cookie);
        }
        renderJson(new BaseResponse(Code.SUCCESS, "退出成功"));
    }

    /**
     * 课程 0 ， 文本资源 1， 视频资源 2
     * id, name, location, type, count, time, user_id
     * 热门资源 热门课程 观看历史 最新资源（写死）
     */
    @Clear(POST.class)
    public void home(){
        // 热门课程
        List<Record> hotCourses = Db.find("select name, location, sum(count) as count from watch where type = 0 group by name,location order by sum(count) desc limit 0, 5");
        // 热门资源
        List<Record> hotResources = Db.find("select name, location, sum(count) as count from watch where type = 1 or type = 2 group by name, location order by sum(count) desc limit 0, 5");
        // 观看历史
        List<Record> history = Db.find("select * from watch where user_id = ? order by time desc limit 0, 5", getSession().getAttribute("username"));
        Record result = new Record();
        result.set("hotCourses", hotCourses).set("hotResources", hotResources).set("history", history);
        renderJson(new BaseResponse(Code.SUCCESS, "返回列表成功", result));
        return;
    }

    //添加watch
    public void watch(){
        String name = getPara(Watch.NAME);
        String location = getPara(Watch.LOCATION);
        int type = getParaToInt(Watch.TYPE);
        int user_id = (Integer) getSession().getAttribute("username");
        if (!notNull(Require.me()
                .put(name, "课程/资源名称不能为空")
                .put(location, "地址不能为空")
                .put(type, "类型不能为空"))){
            return;
        }
        Record record = Db.findFirst("select * from watch where user_id = ? and name = ? and type = ?", user_id, name, type);
        if (record == null){
            Db.update("insert into watch (name, location, type, time, user_id, count) values (?, ?, ?, ?, ?, 1)", name, location, type, DateUtils.currentTimeStamp(), user_id);
        }else {
            Db.update("update watch set count = count + 1 where name = ? and type =? and user_id = ? ", name, type, user_id);
        }
        renderJson(new BaseResponse(Code.SUCCESS, "添加成功"));
    }


    /**
     * saveOrDelete : 0,save; 1,delete
     */
    public void favorite(){
        String name = getPara(Favorite.NAME);
        String location = getPara(Favorite.LOCATION);
        int type = getParaToInt(Favorite.TYPE);
        String image = getPara(Favorite.image);
        String description = getPara(Favorite.description);
        int saveOrDelete = getParaToInt("saveOrDelete");
        int user_id = (Integer) getSession().getAttribute("username");
        if (!notNull(Require.me()
                .put(name, "课程/资源名称不能为空")
                .put(location, "地址不能为空")
                .put(type, "类型不能为空")
                .put(image, "图片地址不能为空")
                .put(description, "描述不能为空"))){
            return;
        }
        Record record = Db.findFirst("select * from favorite where name = ? and type = ? and user_id = ?", name, type, user_id);
        // 判断fav是否已经存在
        if (saveOrDelete == 0){
            if (record == null){
                Db.update("insert into favorite (name, location, type, time, user_id, image, description) values (?, ?, ?, ?, ?, ?, ?)", name, location, type, DateUtils.currentTimeStamp(), user_id, image, description);
            }
            renderJson(new BaseResponse(Code.SUCCESS, "收藏成功!"));
        }else if (saveOrDelete == 1){
            Db.update("delete from favorite where name = ? and type = ? and user_id = ?", name, type, user_id);
            renderJson(new BaseResponse(Code.SUCCESS, "取消收藏成功!"));
        }else{
            renderJson(new BaseResponse(Code.FAILURE, "saveOrDelete参数不正确"));
        }
    }

    public void getFavoriteList(){
        int user_id = (Integer)getSession().getAttribute("username");
        List<Record> list = Db.find("select * from favorite where user_id = ?", user_id);
        renderJson(new BaseResponse(Code.SUCCESS, "收藏列表", list));
    }


}