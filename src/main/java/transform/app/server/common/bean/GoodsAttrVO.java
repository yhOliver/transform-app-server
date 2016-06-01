package transform.app.server.common.bean;

import java.util.List;

/**
 * 商品属性VO
 * Created by wang on 2016/6/1.
 */
public class GoodsAttrVO {
    private String key;
    private List<?> value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<?> getValue() {
        return value;
    }

    public void setValue(List<?> value) {
        this.value = value;
    }
}
