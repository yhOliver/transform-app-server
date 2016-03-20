package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import transform.app.server.common.Require;
import transform.app.server.common.bean.Code;
import transform.app.server.common.bean.DataResponse;
import transform.app.server.common.bean.PageResponse;
import transform.app.server.common.bean.VenueDetailResponse;
import transform.app.server.interceptor.GET;
import transform.app.server.model.SportType;
import transform.app.server.model.Venue;
import transform.app.server.model.VenueSport;

import java.util.List;

import static transform.app.server.model.SportType.SPTY_ID;
import static transform.app.server.model.Venue.VENU_CITY;
import static transform.app.server.model.Venue.VENU_ID;


/**
 * 场馆相关的接口*
 * <p>
 * 获取运动类别:      GET /api/venue/types
 * 分页获取场馆列表: GET /api/venue/venues TODO 修改返回值
 * 场馆详情:         GET /api/venue/detail
 *
 * @author zhuqi259
 */
public class VenueAPIController extends BaseAPIController {
    private static final String defaultCity = "长春";

    @Before(GET.class)
    public void types() {
        List<SportType> sportTypes = SportType.dao.find("select * from tbsport_typedic");
        renderJson(new DataResponse(sportTypes));
    }

    /**
     * 分页获取场馆列表
     * 参数包括：
     * 运动类别 spty_id
     * city   市（没有默认为 长春）
     * 分页参数  int pageNumber, int pageSize
     * <p>
     * TODO 修改返回值
     * 返回值:
     * 名称、距离、等等
     */
    @Before(GET.class)
    public void venues() {
        String spty_id = getPara(SPTY_ID);
        //校验参数, 确保不能为空
        if (!notNull(Require.me().put(spty_id, "sport type id can not be null"))) {
            return;
        }
        int pageNumber = getParaToInt("pageNumber", 0) + 1; // 前台页数从0开始, 此处sql需+1
        int pageSize = getParaToInt("pageSize", 5);
        String venu_city = getPara(VENU_CITY, defaultCity);
        Page<Venue> venuePage = Venue.dao.paginate(pageNumber, pageSize, "select *", "from tbvenue where venu_isonline = 1 and venu_city = ?", venu_city);
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
        int pageSize = getParaToInt("pageSize", 5);
        VenueDetailResponse response = new VenueDetailResponse();
        // 已发布的该类别场馆
        Venue venue = Venue.dao.findFirst("SELECT * FROM tbvenue WHERE venu_id=? AND venu_isonline=1", venu_id);
        if (venue == null) {
            response.setCode(Code.FAIL).setMessage("venue is not existed or offline");
            renderJson(response);
            return;
        }
        List<VenueSport> venueSports = VenueSport.dao.find("SELECT * FROM tbvenue_sport  WHERE venu_id=? AND vesp_isonline=1", venu_id);
        Page<Record> venueComments = Db.paginate(1, pageSize, "select t1.*, t2.user_nickname", "from tbvenue_comment t1, tbuser t2 where t1.user_id=t2.user_id and venu_id=? order by createtime desc", venu_id);
        response.setVenue(venue);
        response.setVenueSports(venueSports);
        response.setVenueComments(venueComments);
        renderJson(response);
    }
}

