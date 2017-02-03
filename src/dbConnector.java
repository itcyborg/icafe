import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by itcyb on 1/2/2017.
 */
public class dbConnector {
    static String details = new getConfig().checkconfigure();
    Connection connection;

    public Connection getConnection() {
        connection = connect();
        return connection;
    }

    private Connection connect() {
        String[] temp;
        String delimiter = ",";
        temp = details.split(delimiter);
        String db = temp[0];
        String pass = temp[1];
        String user = temp[2];
        Connection conn = null;

        //1. allocate a db connection object
        try {
            //allocate a db connection object
            conn = DriverManager.getConnection("jdbc:mysql://" + db, user, pass);
        } catch (CommunicationsException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot Establish Connection to MySQl Database.\n" +
                            "Ensure that it is installed, running and configured correctly\n " +
                            "then restart this application.");
            new logger(e.toString()).writelog();
        } catch (MySQLSyntaxErrorException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot Establish Connection to MySQl Database.\n" +
                            "Ensure that it is installed, running and configured correctly\n " +
                            "then restart this application.");
            //e.printStackTrace();
            new logger(e.toString()).writelog();
            getConfig config = new getConfig();
            config.configure();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot Establish Connection to MySQl Database.\n" +
                            "Ensure that it is installed, running and configured correctly\n " +
                            "then restart this application.");
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return conn;
    }
}
