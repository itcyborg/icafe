import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by itcyb on 11/14/2016.
 */
public class createDB {
    public Boolean createDb(String dbname, String host, String user, String pass) {
        Connection conn = null;

        //1. allocate a db connection object
        try {
            //allocate a db connection object
            conn = DriverManager.getConnection("jdbc:mysql://" + host, user, pass);
            Statement nstmt = null;
            nstmt = conn.createStatement();
            String sqlt = getConfig.checksql(dbname);
            nstmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
            Connection connj = DriverManager.getConnection("jdbc:mysql://" + host + "/" + dbname, user, pass);
            Statement ss = connj.createStatement();
            String del = "#";
            String nsql[] = sqlt.split(del);
            int i = nsql.length;
            for (String aNsql : nsql) {
                ss.executeUpdate(aNsql);
            }
        } catch (CommunicationsException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        } catch (MySQLSyntaxErrorException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return true;
    }
}
