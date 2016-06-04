package transform.app.server.common.bean;

import transform.app.server.model.Goods;

import java.util.List;

/**
 * 商品详情
 */
public class GoodsDetailVO {
    /**
     * 商品基本信息
     */
    private Goods detailedInfo;
    /**
     * 商品属性信息
     */
    private List<?> attrs;

    public Goods getDetailedInfo() {
        return detailedInfo;
    }

    public void setDetailedInfo(Goods detailedInfo) {
        this.detailedInfo = detailedInfo;
    }

    public List<?> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<?> attrs) {
        this.attrs = attrs;
    }
}


