package transform.app.server.common.bean;

/**
 * 返回对象
 *
 * @author zhuqi259
 *         2016-03-23
 */
public class BaseResponse {

    private String success = Code.SUCCESS;

    private String msg;

    private Object result;

    public BaseResponse() {
    }

    public BaseResponse(String success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public BaseResponse(String success, String msg, Object result) {
        this.success = success;
        this.msg = msg;
        this.result = result;
    }

    public String getSuccess() {
        return success;
    }

    public BaseResponse setSuccess(String success) {
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