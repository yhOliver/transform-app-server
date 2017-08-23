package yh.common.bean;

/**
 * 返回对象
 *
 * @author zhuqi259
 *         2016-03-23
 */
public class BaseResponse {

    private String status = Code.SUCCESS;

    private String message;

    private Object data;

    public BaseResponse() {
    }

    public BaseResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public BaseResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public BaseResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public BaseResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public BaseResponse setData(Object data) {
        this.data = data;
        return this;
    }

}