package transform.app.server.common.bean;

import com.jfinal.plugin.activerecord.Record;

import java.util.List;

/**
 * 商品详情表
 */
public class GoodsDetailVO {
    /**
     * 商品基本信息
     */
    private Record detailedInfo;
    /**
     * 商品attr信息
     */
    private List<?> attr;

    public List<?> getAttr() {
        return attr;
    }

    public void setAttr(List<?> attr) {
        this.attr = attr;
    }

    public Record getDetailedInfo() {
        return detailedInfo;
    }

    public void setDetailedInfo(Record detailedInfo) {
        this.detailedInfo = detailedInfo;
    }
}


