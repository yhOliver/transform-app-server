package transform.app.server.common.bean;

/**
 * @author zhuqi259
 *         2016-03-23
 */
public class BaseResponse {

    private boolean success = Code.SUCCESS;

    private String msg;

    private Object result;

    public BaseResponse() {
    }

    public BaseResponse(String msg) {
        this.msg = msg;
    }

    public BaseResponse(boolean success) {
        this.success = success;
    }

    public BaseResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public BaseResponse(String msg, Object result) {
        this.msg = msg;
        this.result = result;
    }

    public BaseResponse(boolean success, String msg, Object result) {
        this.success = success;
        this.msg = msg;
        this.result = result;
    }

    public BaseResponse(Object result) {
        this.result = result;
    }

    public boolean getSuccess() {
        return success;
    }

    public BaseResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BaseResponse setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Object getResult() {
        return result;
    }

    public BaseResponse setResult(Object result) {
        this.result = result;
        return this;
    }

}