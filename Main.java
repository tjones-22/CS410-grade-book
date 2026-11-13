import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private final Connection connection;
    private final Scanner input;
    private String course;
    private int section;
    private String term;

    Main(Connection connection, Scanner input) {
        this.connection = connection;
        this.input = input;
    }

    public static void main(String[] args) {
        try (Connection connection = Database.connect(); Scanner input = new Scanner(System.in)) {
            System.out.println("Connected to " + connection.getCatalog() + "!");
            new Main(connection, input).run();
        } catch (SQLException exception) {
            System.err.println("Could not connect to MySQL: " + exception.getMessage());
            System.exit(1);
        }
    }

    void run() {
        help();
        while (true) {
            System.out.print("gradebook> ");
            if (!input.hasNextLine()) return;
            String command = input.nextLine().trim();
            try {
                switch (command) {
                    case "new-class" -> newClass();
                    case "list-classes" -> listClasses();
                    case "select-class" -> selectClass();
                    case "show-class" -> showClass();
                    case "add-category" -> addCategory();
                    case "add-assignment" -> addAssignment();
                    case "add-student" -> addStudent();
                    case "help" -> help();
                    case "exit", "quit" -> { return; }
                    case "" -> { }
                    default -> System.out.println("Unknown command. Type help for commands.");
                }
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid input: " + exception.getMessage());
            } catch (NoSuchElementException exception) {
                return; // End of input during a prompt; no partial command is saved.
            }
        }
    }

    private static void help() {
        System.out.println("Commands: new-class, list-classes, select-class, show-class,");
        System.out.println("          add-category, add-assignment, add-student, help, exit");
        System.out.println("Enter a command by itself, then answer the prompts.");
    }

    private String ask(String prompt, int maxLength) {
        System.out.print(prompt + ": ");
        String value = input.nextLine().trim();
        if (value.isEmpty() || value.length() > maxLength) {
            throw new IllegalArgumentException(prompt + " must contain 1–" + maxLength + " characters.");
        }
        return value;
    }

    private int positiveInt(String prompt) {
        int value = Integer.parseInt(ask(prompt, 10));
        if (value <= 0) {
            throw new IllegalArgumentException(prompt + " must be positive.");
        }
        return value;
    }

    private BigDecimal decimal(String prompt, int precision, boolean allowZero) {
        BigDecimal value = new BigDecimal(ask(prompt, 30));
        if (value.signum() < 0 || (!allowZero && value.signum() == 0)
                || value.scale() > 2 || value.compareTo(BigDecimal.TEN.pow(precision - 2)) >= 0) {
            throw new IllegalArgumentException(prompt + " must be "
                    + (allowZero ? "nonnegative" : "positive")
                    + ", fit DECIMAL(" + precision + ",2), and have at most two decimal places.");
        }
        return value;
    }

    private void newClass() throws SQLException {
        String newCourse = ask("Course number", 20);
        int newSection = positiveInt("Section number");
        String newTerm = ask("Term", 30);
        String description = ask("Description", 255);
        String category = ask("First category name", 100);
        BigDecimal weight = decimal("First category weight", 5, true);
        Database.createClassWithCategory(newCourse, newSection, newTerm, description, category, weight);
        course = newCourse;
        section = newSection;
        term = newTerm;
        System.out.println("Class created and selected.");
    }

    private void listClasses() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT courseNumber, sectionNumber, term, description FROM Classes"
                + " ORDER BY term, courseNumber, sectionNumber");
             ResultSet rows = statement.executeQuery()) {
            boolean found = false;
            while (rows.next()) {
                found = true;
                System.out.printf("%s | section %d | %s | %s%n", rows.getString(1),
                        rows.getInt(2), rows.getString(3), rows.getString(4));
            }
            if (!found) System.out.println("No classes found.");
        }
    }

    private void selectClass() throws SQLException {
        String newCourse = ask("Course number", 20);
        int newSection = positiveInt("Section number");
        String newTerm = ask("Term", 30);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT courseNumber, sectionNumber, term FROM Classes"
                + " WHERE courseNumber = ? AND sectionNumber = ? AND term = ?")) {
            statement.setString(1, newCourse);
            statement.setInt(2, newSection);
            statement.setString(3, newTerm);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    System.out.println("Class not found; selection unchanged.");
                    return;
                }
                course = rows.getString(1);
                section = rows.getInt(2);
                term = rows.getString(3);
            }
        }
        System.out.println("Selected " + course + " section " + section + " " + term + ".");
    }

    private boolean requireClass() {
        if (course != null) return true;
        System.out.println("Select a class first with select-class or new-class.");
        return false;
    }

    private void bindClass(PreparedStatement statement) throws SQLException {
        statement.setString(1, course);
        statement.setInt(2, section);
        statement.setString(3, term);
    }

    private void showClass() throws SQLException {
        if (!requireClass()) return;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT description FROM Classes"
                + " WHERE courseNumber = ? AND sectionNumber = ? AND term = ?")) {
            bindClass(statement);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    course = null;
                    System.out.println("Selected class no longer exists. Select another class.");
                    return;
                }
                System.out.printf("%s | section %d | %s | %s%n", course, section, term, rows.getString(1));
            }
        }
        printRows("Categories (name | weight)",
                "SELECT name, weight FROM Categories WHERE courseNumber = ? AND sectionNumber = ?"
                + " AND term = ? ORDER BY name");
        printRows("Assignments (name | description | maximum points | category)",
                "SELECT name, description, pValue, categoryName FROM Assignments"
                + " WHERE courseNumber = ? AND sectionNumber = ? AND term = ? ORDER BY name");
        printRows("Students (ID | name | username)",
                "SELECT s.studentID, s.name, s.username FROM Students s"
                + " JOIN Enrolled e ON e.studentID = s.studentID"
                + " WHERE e.courseNumber = ? AND e.sectionNumber = ? AND e.term = ?"
                + " ORDER BY s.name, s.studentID");
    }

    private void printRows(String heading, String sql) throws SQLException {
        System.out.println(heading);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindClass(statement);
            try (ResultSet rows = statement.executeQuery()) {
                boolean found = false;
                int columns = rows.getMetaData().getColumnCount();
                while (rows.next()) {
                    found = true;
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) System.out.print(" | ");
                        System.out.print(rows.getString(i));
                    }
                    System.out.println();
                }
                if (!found) System.out.println("  (none)");
            }
        }
    }

    private void addCategory() throws SQLException {
        if (!requireClass()) return;
        String name = ask("Category name", 100);
        BigDecimal weight = decimal("Weight", 5, true);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Categories (courseNumber, sectionNumber, term, name, weight)"
                + " VALUES (?, ?, ?, ?, ?)")) {
            bindClass(statement);
            statement.setString(4, name);
            statement.setBigDecimal(5, weight);
            statement.executeUpdate();
        }
        System.out.println("Category added.");
    }

    private void addAssignment() throws SQLException {
        if (!requireClass()) return;
        String name = ask("Assignment name", 100);
        String description = ask("Description", 255);
        BigDecimal points = decimal("Maximum points", 8, false);
        String category = ask("Existing category name", 100);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Assignments (courseNumber, sectionNumber, term, name, description,"
                + " pValue, categoryName) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            bindClass(statement);
            statement.setString(4, name);
            statement.setString(5, description);
            statement.setBigDecimal(6, points);
            statement.setString(7, category);
            statement.executeUpdate();
        }
        System.out.println("Assignment added.");
    }

    private void addStudent() throws SQLException {
        if (!requireClass()) return;
        int id = positiveInt("Student ID");
        String name = ask("Student name", 100);
        String username = ask("Username (before @)", 50);
        if (username.contains("@")) throw new IllegalArgumentException("Enter only the email prefix.");
        Database.addStudentToClass(id, name, username, course, section, term);
        System.out.println("Student enrolled in selected class.");
    }
}
