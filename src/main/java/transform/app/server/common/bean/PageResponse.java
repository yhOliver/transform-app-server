package transform.app.server.common.bean;

import com.jfinal.plugin.activerecord.Page;

public class PageResponse<T> extends BaseResponse {
    private Page<T> pageData;

    public Page<T> getPageData() {
        return pageData;
    }

    public PageResponse setPageData(Page<T> pageData) {
        this.pageData = pageData;
        return this;
    }

    public PageResponse() {
        super();
    }

    public PageResponse(Page<T> pageData) {
        this.pageData = pageData;
    }
}
