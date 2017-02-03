import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by itcyb on 1/2/2017.
 */
public class dbPutter {
    Connection connection;
    String query;
    boolean status;

    public dbPutter(Connection connection, String query) {
        this.connection = connection;
        this.query = query;
        status = false;
    }

    protected int putter() {
        int id = 0;
        try {
            //create a statement
            Statement stmt = connection.createStatement();
            //execute the query
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            status = true;
            connection.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return id;
    }

    protected boolean updater() {
        boolean status = false;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            status = true;
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }
}
