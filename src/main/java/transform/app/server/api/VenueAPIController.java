package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.joda.time.DateTime;
import transform.app.server.common.Require;
import transform.app.server.common.bean.*;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.GET;
import transform.app.server.interceptor.POST;
import transform.app.server.model.SportType;
import transform.app.server.model.Venue;
import transform.app.server.model.VenueSport;

import java.util.List;

import static transform.app.server.model.SportType.SPTY_ID;
import static transform.app.server.model.Venue.*;


/**
 * 场馆相关的接口*
 * <p>
 * 获取运动类别:      GET /api/venue/types
 * 分页获取场馆列表: GET /api/venue/venues [缺少按照距离排序]
 * 模糊查询-按照场馆名称或地址查询场馆列表: POST /api/venue/search [缺少按照距离排序]
 * 场馆详情:         GET /api/venue/detail
 * 场馆评价更多分页: GET /api/venue/comments
 *
 * @author zhuqi259
 */
public class VenueAPIController extends BaseAPIController {
    private static final String[] weeks = {"", MON, TUE, WED, THU, FRI, SAT, SUN}; // 1~7
    private static final String defaultCity = "长春市";
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    @Before(GET.class)
    public void types() {
        List<SportType> sportTypes = SportType.dao.find("SELECT * FROM tbsport_typedic");
        renderJson(new DataResponse(sportTypes));
    }

    /**
     * 分页获取场馆列表
     * 参数包括：
     * 运动类别 spty_id (没传参或"0"表示全部)
     * 时间 :  周一 ~~ 周日 (默认是今天，传给我的是1~7中的某个数)
     * 位置(包括两个参数):
     * city   市（没有默认为 长春市）     精确查找=
     * proper 区或者县 (没有则查找全部)   精确查找=
     * 分页参数  int pageNumber, int pageSize
     * <p>
     * <p>
     * 返回值:
     * (全部) => GROUP BY 分组显示
     * (具体类别) => 当前组显示
     * <p>
     * 运动类别名：
     * 场馆第一张宣传图片、名称、地址、距离
     * TODO 按照距离排序
     */
    @Before(GET.class)
    public void venues() {
        String spty_id = getPara(SPTY_ID);
        /**
         * @see org.joda.time.DateTimeConstants.MONDAY
         */
        int defaultWeek = new DateTime().getDayOfWeek();
        int week = getParaToInt("week", defaultWeek);
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            BaseResponse response = new BaseResponse();
            response.setCode(Code.FAIL).setMessage("pageNumber and pageSize must more than 0");
            renderJson(response);
            return;
        }
        String venu_city = getPara(VENU_CITY, defaultCity);
        String venu_proper = getPara(VENU_PROPER);
        StringBuilder sb = new StringBuilder();
        Page<Record> venuePage;
        if (StringUtils.isEmpty(spty_id)) {
            // 查找全部并分组显示
            /**
             SELECT
             dic.*,
             tv.*
             FROM (SELECT * FROM tbvenue WHERE venu_isonline=1 AND venu_city ='长春市') tv
             LEFT JOIN (SELECT venu_id , spty_id FROM tbvenue_sport WHERE vesp_isonline = 1) tvs ON tv.venu_id = tvs.venu_id
             LEFT JOIN  (SELECT * FROM  tbsport_typedic ) dic ON dic.spty_id = tvs.spty_id
             GROUP BY tvs.spty_id, tv.venu_name
             ORDER BY distance
             */

            sb.append("FROM (SELECT * FROM tbvenue WHERE venu_isonline=1 AND venu_city = ? AND ").append(weeks[week]).append("=1 ");
            if (StringUtils.isNotEmpty(venu_proper)) {
                sb.append("AND venu_proper=? ");
            }
            sb.append(") tv ");
            sb.append("LEFT JOIN (SELECT venu_id , spty_id FROM tbvenue_sport WHERE vesp_isonline = 1) tvs ON tv.venu_id = tvs.venu_id ");
            sb.append("LEFT JOIN (SELECT * FROM  tbsport_typedic ) dic ON dic.spty_id = tvs.spty_id ");
            sb.append("GROUP BY tvs.spty_id, tv.venu_name");
            // System.out.println(sb.toString());
            if (StringUtils.isNotEmpty(venu_proper)) {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tv.* ", sb.toString(), venu_city, venu_proper);
            } else {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tv.* ", sb.toString(), venu_city);
            }
        } else {
            // 查看单分组
            /**
             SELECT
             dic.*,
             tv.*
             FROM (SELECT * FROM tbvenue WHERE venu_isonline=1 AND venu_city ='长春市') tv
             LEFT JOIN (SELECT venu_id , spty_id FROM tbvenue_sport WHERE vesp_isonline = 1 AND spty_id='1') tvs ON tv.venu_id = tvs.venu_id
             LEFT JOIN  (SELECT * FROM  tbsport_typedic) dic ON dic.spty_id = tvs.spty_id
             ORDER BY distance
             */
            sb.append("FROM (SELECT * FROM tbvenue WHERE venu_isonline=1 AND venu_city = ? AND ").append(weeks[week]).append("=1 ");
            if (StringUtils.isNotEmpty(venu_proper)) {
                sb.append("AND venu_proper=? ");
            }
            sb.append(") tv ");
            sb.append("LEFT JOIN (SELECT venu_id , spty_id FROM tbvenue_sport WHERE vesp_isonline = 1 AND spty_id=?) tvs ON tv.venu_id = tvs.venu_id ");
            sb.append("LEFT JOIN (SELECT * FROM  tbsport_typedic ) dic ON dic.spty_id = tvs.spty_id ");
            // System.out.println(sb.toString());
            if (StringUtils.isNotEmpty(venu_proper)) {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tv.* ", sb.toString(), venu_city, venu_proper, spty_id);
            } else {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tv.* ", sb.toString(), venu_city, spty_id);
            }
        }
        renderJson(new PageResponse<>(venuePage));
    }

    /**
     * 按照场馆名与地址模糊查询
     * TODO 按照距离排序
     */
    @Before(POST.class)
    public void search() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            BaseResponse response = new BaseResponse();
            response.setCode(Code.FAIL).setMessage("pageNumber and pageSize must more than 0");
            renderJson(response);
            return;
        }
        String wd = getPara("wd");
        if (!notNull(Require.me().put(wd, "search word can not be null"))) {
            return;
        }
        wd = "%" + wd + "%";
        Page<Venue> venuePage = Venue.dao.paginate(pageNumber, pageSize, "SELECT *", "FROM tbvenue WHERE venu_name LIKE ? or venu_address LIKE ?", wd, wd);
        renderJson(new PageResponse<>(venuePage));
    }

    /**
     * 场馆详情页
     */
    @Before(GET.class)
    public void detail() {
        String venu_id = getPara(VENU_ID);
        //校验参数, 确保不能为空
        if (!notNull(Require.me().put(venu_id, "venue id can not be null"))) {
            return;
        }
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        VenueDetailResponse response = new VenueDetailResponse();
        // 已发布的该类别场馆
        Venue venue = Venue.dao.findFirst("SELECT * FROM tbvenue WHERE venu_id=? AND venu_isonline=1", venu_id);
        if (venue == null) {
            response.setCode(Code.FAIL).setMessage("venue is not existed or offline");
            renderJson(response);
            return;
        }
        List<VenueSport> venueSports = VenueSport.dao.find("SELECT * FROM tbvenue_sport  WHERE venu_id=? AND vesp_isonline=1", venu_id);
        // 默认直接第1页
        Page<Record> venueComments = Db.paginate(1, pageSize, "SELECT tc.*, tu.user_nickname", "FROM(SELECT * FROM tbvenue_comment WHERE venu_id=?) tc LEFT JOIN tbuser tu ON tc.user_id=tu.user_id ORDER BY tc.createtime DESC", venu_id);
        response.setVenue(venue);
        response.setVenueSports(venueSports);
        response.setVenueComments(venueComments);
        renderJson(response);
    }

    /**
     * 场馆评价更多分页
     */
    @Before(GET.class)
    public void comments() {
        String venu_id = getPara(VENU_ID);
        //校验参数, 确保不能为空
        if (!notNull(Require.me().put(venu_id, "venue id can not be null"))) {
            return;
        }
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            BaseResponse response = new BaseResponse();
            response.setCode(Code.FAIL).setMessage("pageNumber and pageSize must more than 0");
            renderJson(response);
            return;
        }
        // 已发布的该类别场馆
        Venue venue = Venue.dao.findFirst("SELECT * FROM tbvenue WHERE venu_id=? AND venu_isonline=1", venu_id);
        if (venue == null) {
            BaseResponse response = new BaseResponse();
            response.setCode(Code.FAIL).setMessage("venue is not existed or offline");
            renderJson(response);
            return;
        }
        /**
         SELECT tc.*, tu.user_nickname
         FROM(SELECT * FROM tbvenue_comment WHERE venu_id = '124') tc LEFT JOIN tbuser tu ON tc.user_id = tu.user_id   ORDER BY  tc.createtime DESC
         */
        Page<Record> venueComments = Db.paginate(pageNumber, pageSize, "SELECT tc.*, tu.user_nickname", "FROM(SELECT * FROM tbvenue_comment WHERE venu_id=?) tc LEFT JOIN tbuser tu ON tc.user_id=tu.user_id ORDER BY tc.createtime DESC", venu_id);
        renderJson(new PageResponse<>(venueComments));
    }
}

