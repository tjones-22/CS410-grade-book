import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/university";

    public static Connection connect() throws SQLException {
        String username = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");

        return DriverManager.getConnection(URL, username, password);
    }
}
