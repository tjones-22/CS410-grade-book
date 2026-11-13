
CREATE DATABASE university;
USE university;


CREATE TABLE Students (
    studentID INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    CONSTRAINT uq_students_username UNIQUE (username),
    CONSTRAINT ck_students_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_students_username CHECK (CHAR_LENGTH(TRIM(username)) > 0 AND username NOT LIKE '%@%')
) ENGINE=InnoDB;

CREATE TABLE Classes (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,

    PRIMARY KEY (courseNumber, sectionNumber, term),
    CONSTRAINT ck_classes_section CHECK (sectionNumber > 0),
    CONSTRAINT ck_classes_course CHECK (CHAR_LENGTH(TRIM(courseNumber)) > 0),
    CONSTRAINT ck_classes_term CHECK (CHAR_LENGTH(TRIM(term)) > 0)
) ENGINE=InnoDB;

CREATE TABLE Categories (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    CONSTRAINT ck_categories_weight CHECK (weight >= 0),
    CONSTRAINT ck_categories_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),

    PRIMARY KEY (courseNumber, sectionNumber, term, name),

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Assignments (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    pValue DECIMAL(8,2) NOT NULL,
    CONSTRAINT ck_assignments_value CHECK (pValue > 0),
    CONSTRAINT ck_assignments_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    categoryName VARCHAR(100) NOT NULL,

    PRIMARY KEY (courseNumber, sectionNumber, term, name),

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term)
        ON DELETE RESTRICT ON UPDATE RESTRICT,

    FOREIGN KEY (courseNumber, sectionNumber, term, categoryName)
        REFERENCES Categories(courseNumber, sectionNumber, term, name)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Enrolled (
    studentID INT NOT NULL,
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,

    PRIMARY KEY (studentID, courseNumber, sectionNumber, term),

    FOREIGN KEY (studentID)
        REFERENCES Students(studentID)
        ON DELETE RESTRICT ON UPDATE RESTRICT,

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Grades (
    studentID INT NOT NULL,
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    assignmentName VARCHAR(100) NOT NULL,
    points DECIMAL(8,2) NOT NULL,
    CONSTRAINT ck_grades_points CHECK (points >= 0),

    PRIMARY KEY (
        studentID,
        courseNumber,
        sectionNumber,
        term,
        assignmentName
    ),

    FOREIGN KEY (studentID)
        REFERENCES Students(studentID)
        ON DELETE RESTRICT ON UPDATE RESTRICT,

    FOREIGN KEY (
        courseNumber,
        sectionNumber,
        term,
        assignmentName
    )
        REFERENCES Assignments(
            courseNumber,
            sectionNumber,
            term,
            name
        )
        ON DELETE RESTRICT ON UPDATE RESTRICT,

    FOREIGN KEY (
        studentID,
        courseNumber,
        sectionNumber,
        term
    )
        REFERENCES Enrolled(
            studentID,
            courseNumber,
            sectionNumber,
            term
        )
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_students_name ON Students (name, studentID);

-- List classes in term/course/section order, as used by list-classes.
CREATE INDEX idx_classes_term ON Classes (term, courseNumber, sectionNumber);
