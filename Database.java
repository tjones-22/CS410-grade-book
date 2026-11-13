import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/university";

    public static Connection connect() throws SQLException {
        String username = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");

        return DriverManager.getConnection(System.getenv().getOrDefault("DB_URL", URL), username, password);
    }

    // Reuse an existing student without overwriting their identity; enrollment is atomic.
    public static void addStudentToClass(int studentID, String name, String username,
            String courseNumber, int sectionNumber, String term) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement find = connection.prepareStatement(
                    "SELECT name, username FROM Students WHERE studentID = ? FOR UPDATE");
                 PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO Students (studentID, name, username) VALUES (?, ?, ?)");
                 PreparedStatement enroll = connection.prepareStatement(
                    "INSERT INTO Enrolled (studentID, courseNumber, sectionNumber, term)"
                    + " VALUES (?, ?, ?, ?)")) {
                find.setInt(1, studentID);
                boolean exists;
                try (ResultSet rows = find.executeQuery()) {
                    exists = rows.next();
                    if (exists && (!name.equals(rows.getString(1)) || !username.equals(rows.getString(2)))) {
                        throw new SQLException("Student ID already exists with different name or username.");
                    }
                }
                if (!exists) {
                    insert.setInt(1, studentID);
                    insert.setString(2, name);
                    insert.setString(3, username);
                    insert.executeUpdate();
                }
                enroll.setInt(1, studentID);
                enroll.setString(2, courseNumber);
                enroll.setInt(3, sectionNumber);
                enroll.setString(4, term);
                enroll.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    // D2: a class and its first category must become visible together.
    public static void createClassWithCategory(String courseNumber, int sectionNumber,
            String term, String description, String categoryName, BigDecimal weight)
            throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insertClass = connection.prepareStatement(
                    "INSERT INTO Classes (courseNumber, sectionNumber, term, description)"
                    + " VALUES (?, ?, ?, ?)");
                 PreparedStatement insertCategory = connection.prepareStatement(
                    "INSERT INTO Categories (courseNumber, sectionNumber, term, name, weight)"
                    + " VALUES (?, ?, ?, ?, ?)")) {
                insertClass.setString(1, courseNumber);
                insertClass.setInt(2, sectionNumber);
                insertClass.setString(3, term);
                insertClass.setString(4, description);
                insertClass.executeUpdate();

                insertCategory.setString(1, courseNumber);
                insertCategory.setInt(2, sectionNumber);
                insertCategory.setString(3, term);
                insertCategory.setString(4, categoryName);
                insertCategory.setBigDecimal(5, weight);
                insertCategory.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    // Lock the class so concurrent deletions cannot both remove the remaining categories.
    public static void deleteCategory(String courseNumber, int sectionNumber,
            String term, String categoryName) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement lockClass = connection.prepareStatement(
                    "SELECT courseNumber FROM Classes"
                    + " WHERE courseNumber = ? AND sectionNumber = ? AND term = ? FOR UPDATE");
                 PreparedStatement categories = connection.prepareStatement(
                    "SELECT name FROM Categories"
                    + " WHERE courseNumber = ? AND sectionNumber = ? AND term = ? FOR UPDATE");
                 PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM Categories WHERE courseNumber = ? AND sectionNumber = ?"
                    + " AND term = ? AND name = ?")) {
                for (PreparedStatement statement : new PreparedStatement[] {lockClass, categories, delete}) {
                    statement.setString(1, courseNumber);
                    statement.setInt(2, sectionNumber);
                    statement.setString(3, term);
                }
                try (ResultSet result = lockClass.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Class does not exist.");
                    }
                }
                int count = 0;
                try (ResultSet result = categories.executeQuery()) {
                    while (result.next()) {
                        count++;
                    }
                }
                if (count <= 1) {
                    throw new SQLException("A class must keep at least one category.");
                }
                delete.setString(4, categoryName);
                if (delete.executeUpdate() != 1) {
                    throw new SQLException("Category does not exist.");
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
