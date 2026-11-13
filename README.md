# CS 410 - Gradebook

## Compile and run

Requires Java 17+, MySQL running with the `university` database, and the MySQL
Connector/J JAR in `lib/`.

Each developer must run this once on their own MySQL server, from the project
directory, to create `university` and its tables:

```bash
mysql -u root -p < schema.sql
```

Enter your MySQL root password when prompted. The Java application connects to
the database; it does not create it. Skip this step if the database is already set up.

Set `DB_USER` and `DB_PASSWORD` to your MySQL credentials, then compile and run
from the project directory:

```bash
export DB_USER='root'
export DB_PASSWORD='your-actual-mysql-password'
javac Main.java Database.java
java -cp ".:lib/*" Main
```

The default connection is `jdbc:mysql://127.0.0.1:3306/university`.
Set `DB_URL` to use another JDBC URL.

## Commands

Enter a command by itself, then answer the prompts. Names and descriptions may
contain spaces.

| Command | Usage |
| --- | --- |
| `new-class` | Enter course number, section, term, description, first category, and weight. Creates and selects the class. |
| `list-classes` | List all classes. |
| `select-class` | Select a class by course number, section, and term. |
| `show-class` | Show the selected class, categories, assignments, and enrolled students. |
| `add-category` | Enter a category name and nonnegative weight for the selected class. |
| `add-assignment` | Enter a name, description, positive maximum points, and existing category name. |
| `add-student` | Enter student ID, name, and username to enroll a student in the selected class. For an existing ID, enter the same name and username. |
| `help` | Display commands. |
| `exit` / `quit` | Close the application. |

Select or create a class before using `show-class`, `add-category`,
`add-assignment`, or `add-student`. Enter usernames without the `@` domain.

Example (one line per prompt):

```text
new-class
CS410
1
Fa26
Databases
Homework
40
list-classes
show-class
exit
```
