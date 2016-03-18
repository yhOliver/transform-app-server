package transform.app.server.common.token;

import transform.app.server.common.utils.TokenUtil;
import transform.app.server.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author malongbo
 * @date 15-1-18
 * @package com.pet.project.common.token
 */
public class TokenManager {
    private static TokenManager me = new TokenManager();

    private Map<String, User> tokens;
    private Map<String, String> userToken;

    public TokenManager() {
        tokens = new ConcurrentHashMap<>();
        userToken = new ConcurrentHashMap<>();
    }

    /**
     * 获取单例对象
     *
     * @return TokenManager
     */
    public static TokenManager getMe() {
        return me;
    }

    /**
     * 验证token
     *
     * @param token String
     * @return User
     */
    public User validate(String token) {
        return tokens.get(token);
    }

    /**
     * 生成token值
     *
     * @param user User
     * @return String
     */
    public String generateToken(User user) {
        String token = TokenUtil.generateToken();
        userToken.put(user.getStr(User.USER_ID), token);
        tokens.put(token, user);
        return token;
    }
}
