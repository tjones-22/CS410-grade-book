import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT DATABASE()")) {

            if (result.next()) {
                System.out.println("Connected to " + result.getString(1) + "!");
            }
        } catch (SQLException exception) {
            System.err.println("Could not connect to MySQL: " + exception.getMessage());
            System.err.println("Check that MySQL is running on port 3306, university exists,"
                    + " and MySQL Connector/J is on the classpath.");
            System.exit(1);
        }
    }
}
