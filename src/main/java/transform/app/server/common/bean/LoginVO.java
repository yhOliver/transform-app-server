package transform.app.server.common.bean;

import java.util.Map;

/**
 * @author zhuqi259
 *         2016-03-23
 */
public class LoginVO {
    private Map<String, Object> info;
    private String token;
    private Constant constant = Constant.me();

    public Map<String, Object> getInfo() {
        return info;
    }

    public LoginVO setInfo(Map<String, Object> info) {
        this.info = info;
        return this;
    }

    public String getToken() {
        return token;
    }

    public LoginVO setToken(String token) {
        this.token = token;
        return this;
    }

    public Constant getConstant() {
        return constant;
    }

    public LoginVO setConstant(Constant constant) {
        this.constant = constant;
        return this;
    }
}