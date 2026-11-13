# CS 410 - Gradebook

## How to run

To make the univeesity database run:

```

mysql -u root -p < schema.sql
```

You will need to set your DB_USER and DB_PASSWORD yourself:

```

export DB_USER='root'
export DB_PASSWORD='your-mysql-password'

```


To Compile, run the following command:

```

javac Main.java Database.java
java -cp ".:lib/*" Main
```
## Tables

D1 · Classes
The system tracks many classes. A class is identified to a user by its course number (e.g. CS410), term (e.g. Fa26), and section number (e.g. 1). No two classes share all three. A class also has a description (e.g. Databases). You may add fields you can justify.

D2 · Categories
A class has one or more grading categories — homework, exam, project, participation, and so on. Each category has a name and a weight. Category names are unique within a class, and two different classes may use different category names and weights. Weights are not required to sum to 100; see D8.

D3 · Assignments
A class has assignments. Each belongs to exactly one category of that class, and has a name, a description, and a point value (the maximum points available). No two assignments in the same class may share a name; assignments in different classes may.

D4 · Students
A student has a username (the part of their email address before the @), a student ID, and a name. A student exists independently of any class and may be enrolled in many classes across many terms. Usernames and student IDs are each unique across the whole system.

D5 · Enrollment
A student is enrolled in zero or more classes; a class enrolls zero or more students. A student cannot be enrolled in the same class twice.

D6 · Grades
A student who is enrolled in a class may be assigned a grade — a number of points — for an assignment in that class. A student has at most one grade per assignment. A student with no grade for an assignment is ungraded, which is not the same as scoring zero: the distinction drives §4's two grade figures, so do not represent it with a 0.

D7 · Grade integrity
It must be impossible for the database to hold a grade for a student who is not enrolled in the class that owns the assignment. Enforce this with constraints where you can; where the constraint cannot be expressed declaratively, say so in your README and explain how your application prevents it.

D8 · Grade calculation
Grades are reported out of 100 using the weighted scheme fully specified in §4

##  Milestone 2

Part 2 — MySQL schema
Produce schema.sql: a script that creates your database from nothing. It must run cleanly on a fresh MySQL server with mysql < schema.sql and must include:

CREATE TABLE statements with appropriate data types. Use DECIMAL for point values and grades, not FLOAT or DOUBLE.
PRIMARY KEY, FOREIGN KEY, UNIQUE, NOT NULL, and CHECK constraints implementing D1–D7. Choose ON DELETE / ON UPDATE behavior deliberately for each foreign key.
At least two secondary indexes beyond those created automatically by keys, each with a one-line comment in the script saying which query it is there to serve.
Any views you use. Views are encouraged for the reporting queries in.

