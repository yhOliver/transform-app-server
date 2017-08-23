package yh.common.token;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import yh.common.utils.TokenUtil;
import yh.model.User;

/**
 * @author zhuqi259
 *         <p>
 *         token管理器
 */
public class TokenManager {
    private static TokenManager me = new TokenManager();

    private TokenManager() {
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
        /**
         * SELECT * FROM t_token WHERE token = ? AND UNIX_TIMESTAMP(deadtime) > UNIX_TIMESTAMP()
         * token没有过期
         */
        Record record = Db.findFirst("SELECT * FROM t_token WHERE token = ? AND UNIX_TIMESTAMP(deadtime) > UNIX_TIMESTAMP()", token);
        if (record == null) {
            return null;
        } else {
            return User.dao.findFirst("SELECT * FROM tbuser WHERE user_id=? AND status=1", record.getStr(User.USER_ID));
        }
    }

    /**
     * 生成token值
     *
     * @param user User
     * @return String
     */
    public String generateToken(final User user) {
        final String token = TokenUtil.generateToken();
        // 数据库新增token
       /* Db.tx(new IAtom() {
                  public boolean run() throws SQLException {
                      *//*return new Token()
                              .set(TOKEN, token)
                              .set(Token.USER_ID, user.userId())
                              .set(DEADTIME, DateUtils.getOneMonthLaterTime())
                              .save();*//*
                  }
              }
        );*/
        return token;
    }
}
