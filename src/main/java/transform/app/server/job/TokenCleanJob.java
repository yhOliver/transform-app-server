package transform.app.server.job;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;

public class TokenCleanJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("TokenCleanJob start -> " + System.currentTimeMillis());
        Db.tx(new IAtom() {
                  public boolean run() throws SQLException {
                      Db.update("DELETE FROM t_token WHERE UNIX_TIMESTAMP(deadtime) < UNIX_TIMESTAMP()");
                      return true;
                  }
              }
        );
        System.out.println("TokenCleanJob end -> " + System.currentTimeMillis());
    }

}
