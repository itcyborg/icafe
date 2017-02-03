import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by itcyb on 1/2/2017.
 */
public class dbGetter {
    String query;
    Connection connection;

    public dbGetter(Connection connection, String query) {
        this.query = query;
        this.connection = connection;
    }

    public ResultSet getter() {
        ResultSet resultSet = null;
        try {
            Statement stmt = connection.createStatement();
            resultSet = stmt.executeQuery(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return resultSet;
    }

    public boolean verifyuser() {
        boolean status = false;
        try {
            ResultSet resultSet = null;
            Statement stmt = connection.createStatement();
            resultSet = stmt.executeQuery(query);
            int count = 0;
            while (resultSet.next()) {
                count++;
            }
            if (count == 1) {
                status = true;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }
}
