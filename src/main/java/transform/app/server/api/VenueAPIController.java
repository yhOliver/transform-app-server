package transform.app.server.api;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.joda.time.DateTime;
import transform.app.server.common.Require;
import transform.app.server.common.bean.*;
import transform.app.server.common.utils.DateUtils;
import transform.app.server.common.utils.MapUtils;
import transform.app.server.common.utils.StringUtils;
import transform.app.server.interceptor.DeviceInterceptor;
import transform.app.server.interceptor.POST;
import transform.app.server.interceptor.VenueStatusInterceptor;
import transform.app.server.model.Distance;
import transform.app.server.model.SportType;
import transform.app.server.model.Venue;
import transform.app.server.model.VenueSport;

import java.util.ArrayList;
import java.util.List;

import static transform.app.server.model.Distance.*;
import static transform.app.server.model.Distance.VENU_ID;
import static transform.app.server.model.SportType.SPTY_ID;
import static transform.app.server.model.Venue.*;


/**
 * 场馆相关的接口*
 * <p>
 * 获取运动类别:      POST /api/venue/types
 * 分页获取场馆列表: POST /api/venue/venues
 * 模糊查询-按照场馆名称或地址查询场馆列表: POST /api/venue/search
 * 场馆详情:         POST /api/venue/detail
 * 场馆评价更多分页: POST /api/venue/comments
 *
 * @author zhuqi259
 */
@Before({POST.class, DeviceInterceptor.class})
public class VenueAPIController extends BaseAPIController {
    private static final String[] weeks = {"", MON, TUE, WED, THU, FRI, SAT, SUN}; // 1~7
    private static final String defaultCity = "长春市";
    private static final int defaultPageNumber = 1;
    private static final int defaultPageSize = 5;

    /**
     * 获取运动类别
     * <p>
     * POST
     */
    @Clear
    @Before(POST.class)
    public void types() {
        List<SportType> sportTypes = SportType.dao.find("SELECT * FROM tbsport_typedic");
        renderJson(new BaseResponse(sportTypes));
    }

    /**
     * 分页获取场馆列表
     * 参数包括：
     * 运动类别 spty_id (没传参表示全部)
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
     * 运动类别名、场馆第一张宣传图片、名称、地址、距离
     * => 已完成按照距离排序
     * <p>
     * 需要计算距离=>
     * <p>
     * POST、设备号、事务（更新距离使用）
     */
    @Before(Tx.class)
    public void venues() {
        String device_uuid = getPara(DEVICE_UUID);
        String device_longitude = getPara("device_longitude");
        String device_latitude = getPara("device_latitude");
        String spty_id = getPara(SPTY_ID);
        /**
         * @see org.joda.time.DateTimeConstants.MONDAY
         */
        int defaultWeek = new DateTime().getDayOfWeek();
        int week = getParaToInt("week", defaultWeek);
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String venu_city = getPara(VENU_CITY, defaultCity);
        String venu_proper = getPara(VENU_PROPER);

        if (StringUtils.isNotEmpty(device_longitude) && StringUtils.isNotEmpty(device_latitude)) {
            // 设备距离参数存在 => 更新距离数据
            try {
                Double longitude = Double.parseDouble(device_longitude);
                Double latitude = Double.parseDouble(device_latitude);
                StringBuilder sb = new StringBuilder();
                List<Record> venues;
                /**
                 *  更新关联的场馆距离(city范围内的均改变，省略重复计算)
                 */
                sb.append("SELECT venu_id, venu_longitude, venu_latitude FROM tbvenue WHERE venu_isonline=1 AND venu_city = ? AND ").append(weeks[week]).append("=1 ");
                if (StringUtils.isNotEmpty(venu_proper)) {
                    sb.append("AND venu_proper=? ");
                    venues = Db.find(sb.toString(), venu_city, venu_proper);
                } else {
                    venues = Db.find(sb.toString(), venu_city);
                }
                calcDistances(device_uuid, longitude, latitude, venues);
            } catch (NumberFormatException ex) {
                renderFailed("device longitude and latitude must be String and double_parseable");
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        Page<Record> venuePage;
        if (StringUtils.isEmpty(spty_id)) {
            // 查找全部并分组显示
            /**
             SELECT
             dic.*, tvd.*
             FROM
             (
             SELECT DISTINCT
             spty_id,
             venu_id
             FROM
             tbvenue_sport
             WHERE
             vesp_isonline = 1
             ) tvs
             LEFT JOIN (
             SELECT
             tv.*, td.device_uuid,
             td.dv_distance
             FROM
             (
             SELECT
             *
             FROM
             tbvenue
             WHERE
             venu_isonline = 1
             AND venu_city = '长春市'
             ) tv
             LEFT JOIN (
             SELECT
             venu_id,
             device_uuid,
             dv_distance
             FROM
             t_distance
             WHERE
             device_uuid = 'a-1234567765'
             ) td ON tv.venu_id = td.venu_id
             ) tvd ON tvd.venu_id = tvs.venu_id
             LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id
             GROUP BY
             tvs.spty_id,
             tvd.venu_id
             ORDER BY
             tvs.spty_id,
             tvd.dv_distance


             即
             SELECT dic.*, tvd.*
             FROM (SELECT DISTINCT spty_id, venu_id FROM tbvenue_sport WHERE vesp_isonline = 1) tvs
             LEFT JOIN (SELECT tv.*, td.device_uuid, td.dv_distance FROM (SELECT * FROM tbvenue WHERE venu_isonline = 1 AND venu_city = '长春市') tv LEFT JOIN (SELECT venu_id, device_uuid, dv_distance FROM t_distance WHERE device_uuid = 'a-1234567765') td ON tv.venu_id = td.venu_id) tvd ON tvd.venu_id = tvs.venu_id
             LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id
             GROUP BY tvs.spty_id, tvd.venu_id
             ORDER BY tvs.spty_id, tvd.dv_distance
             */
            sb.append("FROM (SELECT DISTINCT spty_id, venu_id FROM tbvenue_sport WHERE vesp_isonline = 1) tvs ");
            sb.append("LEFT JOIN (SELECT tv.*, td.device_uuid, td.dv_distance FROM (SELECT * FROM tbvenue WHERE venu_isonline = 1 AND venu_city = ? AND ").append(weeks[week]).append("=1 ");
            if (StringUtils.isNotEmpty(venu_proper)) {
                sb.append("AND venu_proper=? ");
            }
            sb.append(") tv LEFT JOIN (SELECT venu_id, device_uuid, dv_distance FROM t_distance WHERE device_uuid = ?) td ON tv.venu_id = td.venu_id) tvd ON tvd.venu_id = tvs.venu_id ");
            sb.append("LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id ");
            sb.append("GROUP BY tvs.spty_id, tvd.venu_id ");
            sb.append("ORDER BY tvs.spty_id, tvd.dv_distance ");
            // System.out.println(sb.toString());
            if (StringUtils.isNotEmpty(venu_proper)) {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tvd.* ", sb.toString(), venu_city, venu_proper, device_uuid);
            } else {
                venuePage = Db.paginate(pageNumber, pageSize, true, "SELECT dic.*, tvd.* ", sb.toString(), venu_city, device_uuid);
            }
        } else {
            // 查看单分组
            /**
             SELECT
             dic.*, tvd.*
             FROM
             (
             SELECT DISTINCT
             venu_id,
             spty_id
             FROM
             tbvenue_sport
             WHERE
             vesp_isonline = 1
             AND spty_id = '1'
             ) tvs
             LEFT JOIN (
             SELECT
             tv.*, td.device_uuid,
             td.dv_distance
             FROM
             (
             SELECT
             *
             FROM
             tbvenue
             WHERE
             venu_isonline = 1
             AND venu_city = '长春市'
             ) tv
             LEFT JOIN (
             SELECT
             venu_id,
             device_uuid,
             dv_distance
             FROM
             t_distance
             WHERE
             device_uuid = 'a-1234567765'
             ) td ON tv.venu_id = td.venu_id
             ) tvd ON tvd.venu_id = tvs.venu_id
             LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id
             ORDER BY
             tvd.dv_distance

             即
             SELECT dic.*, tvd.*
             FROM (SELECT DISTINCT venu_id, spty_id FROM tbvenue_sport WHERE vesp_isonline = 1 AND spty_id = '1') tvs
             LEFT JOIN (SELECT tv.*, td.device_uuid, td.dv_distance FROM (SELECT * FROM tbvenue WHERE venu_isonline = 1 AND venu_city = '长春市') tv LEFT JOIN (SELECT venu_id, device_uuid, dv_distance FROM t_distance WHERE device_uuid = 'a-1234567765') td ON tv.venu_id = td.venu_id) tvd ON tvd.venu_id = tvs.venu_id
             LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id
             ORDER BY tvd.dv_distance
             */
            sb.append("FROM (SELECT DISTINCT venu_id, spty_id FROM tbvenue_sport WHERE vesp_isonline = 1 AND spty_id = ?) tvs ");
            sb.append("LEFT JOIN (SELECT tv.*, td.device_uuid, td.dv_distance FROM (SELECT * FROM tbvenue WHERE venu_isonline = 1 AND venu_city = ? AND ").append(weeks[week]).append("=1 ");
            if (StringUtils.isNotEmpty(venu_proper)) {
                sb.append("AND venu_proper=? ");
            }
            sb.append(") tv LEFT JOIN (SELECT venu_id, device_uuid, dv_distance FROM t_distance WHERE device_uuid = ?) td ON tv.venu_id = td.venu_id) tvd ON tvd.venu_id = tvs.venu_id ");
            sb.append("LEFT JOIN (SELECT * FROM tbsport_typedic) dic ON dic.spty_id = tvs.spty_id ");
            sb.append("ORDER BY tvd.dv_distance ");
            // System.out.println(sb.toString());
            if (StringUtils.isNotEmpty(venu_proper)) {
                venuePage = Db.paginate(pageNumber, pageSize, "SELECT dic.*, tvd.* ", sb.toString(), spty_id, venu_city, venu_proper, device_uuid);
            } else {
                venuePage = Db.paginate(pageNumber, pageSize, "SELECT dic.*, tvd.* ", sb.toString(), spty_id, venu_city, device_uuid);
            }
        }
        renderJson(new BaseResponse(venuePage));
    }

    /**
     * 按照场馆名与地址模糊查询
     * => 已经按照距离排序
     * <p>
     * 同venues，
     * POST、设备号、事务（更新距离使用）
     */
    @Before(Tx.class)
    public void search() {
        String device_uuid = getPara(DEVICE_UUID);
        String device_longitude = getPara("device_longitude");
        String device_latitude = getPara("device_latitude");
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }
        String wd = getPara("wd");
        if (!notNull(Require.me().put(wd, "search word can not be null"))) {
            return;
        }
        wd = "%" + wd + "%";
        if (StringUtils.isNotEmpty(device_longitude) && StringUtils.isNotEmpty(device_latitude)) {
            // 设备距离参数存在 => 更新距离数据
            try {
                Double longitude = Double.parseDouble(device_longitude);
                Double latitude = Double.parseDouble(device_latitude);
                List<Record> venues = Db.find("SELECT venu_id, venu_longitude, venu_latitude FROM tbvenue WHERE venu_isonline=1 AND venu_name LIKE ? OR venu_address LIKE ?", wd, wd);
                calcDistances(device_uuid, longitude, latitude, venues);
            } catch (NumberFormatException ex) {
                renderFailed("device longitude and latitude must be String and double_parseable");
                return;
            }
        }
        // 按照距离排序
        // 距离表中存在的才应该显示出来~~
        Page<Record> venuePage = Db.paginate(pageNumber, pageSize, "SELECT td.dv_distance, tv.venu_id, tv.venu_name, tv.venu_address, tv.img0", "FROM (SELECT * FROM t_distance WHERE device_uuid=? ) td LEFT JOIN tbvenue tv ON td.venu_id = tv.venu_id ORDER BY td.dv_distance", device_uuid);
        renderJson(new BaseResponse(venuePage));
    }

    /**
     * 场馆详情页
     * <p>
     * POST、检查场馆存在并上线
     */
    @Clear
    @Before({POST.class, VenueStatusInterceptor.class})
    public void detail() {
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageSize < 1) {
            renderFailed("pageSize must more than 0");
            return;
        }
        Venue venue = getAttr("venue");
        String venu_id = venue.getStr(Venue.VENU_ID);

        VenueDetailVO vo = new VenueDetailVO();
        List<VenueSport> venueSports = VenueSport.dao.find("SELECT * FROM tbvenue_sport  WHERE venu_id=? AND vesp_isonline=1", venu_id);
        // 默认直接第1页
        Page<Record> venueComments = Db.paginate(1, pageSize, "SELECT tc.*, tu.user_nickname", "FROM(SELECT * FROM tbvenue_comment WHERE venu_id=?) tc LEFT JOIN tbuser tu ON tc.user_id=tu.user_id ORDER BY tc.createtime DESC", venu_id);
        vo.setVenue(venue);
        vo.setVenueSports(venueSports);
        vo.setVenueComments(venueComments);
        renderJson(new BaseResponse(vo));
    }

    /**
     * 场馆评价更多分页
     * <p>
     * POST、检查场馆存在并上线
     */
    @Clear
    @Before({POST.class, VenueStatusInterceptor.class})
    public void comments() {
        int pageNumber = getParaToInt("pageNumber", defaultPageNumber); // 页数从1开始
        int pageSize = getParaToInt("pageSize", defaultPageSize);
        if (pageNumber < 1 || pageSize < 1) {
            renderFailed("pageNumber and pageSize must more than 0");
            return;
        }

        Venue venue = getAttr("venue");
        String venu_id = venue.getStr(Venue.VENU_ID);
        /**
         SELECT tc.*, tu.user_nickname
         FROM(SELECT * FROM tbvenue_comment WHERE venu_id = '124') tc LEFT JOIN tbuser tu ON tc.user_id = tu.user_id   ORDER BY  tc.createtime DESC
         */
        Page<Record> venueComments = Db.paginate(pageNumber, pageSize, "SELECT tc.*, tu.user_nickname", "FROM(SELECT * FROM tbvenue_comment WHERE venu_id=?) tc LEFT JOIN tbuser tu ON tc.user_id=tu.user_id ORDER BY tc.createtime DESC", venu_id);
        renderJson(new BaseResponse(venueComments));
    }

    private void calcDistances(String device_uuid, double device_longitude, double device_latitude, List<Record> venues) {
        //删除设备历史距离记录
        Db.update("DELETE FROM t_distance WHERE device_uuid=?", device_uuid);
        // 计算新的距离值
        List<Distance> distances = new ArrayList<>();
        for (Record venue : venues) {
            double dv_distance = MapUtils.LantitudeLongitudeDist(device_longitude, device_latitude, venue.getDouble(VENU_LONGITUDE), venue.getDouble(VENU_LATITUDE));
            Distance distance = new Distance()
                    .set(DEVICE_UUID, device_uuid)
                    .set(Distance.VENU_ID, venue.getStr(Venue.VENU_ID))
                    .set(Distance.DV_DISTANCE, dv_distance)
                    .set(Distance.UPDATETIME, DateUtils.currentTimeStamp());
            distances.add(distance);
        }
        Db.batchSave(distances, 1000); //批量保存
    }
}

