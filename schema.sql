CREATE DATABASE university;
USE university;


CREATE TABLE Students (
    studentID INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL
);

CREATE TABLE Classes (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    description VARCHAR(255),

    PRIMARY KEY (courseNumber, sectionNumber, term)
);

CREATE TABLE Categories (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    weight DECIMAL(5,2),

    PRIMARY KEY (courseNumber, sectionNumber, term, name),

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term)
);

CREATE TABLE Assignments (
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    pValue DECIMAL(8,2),
    categoryName VARCHAR(100) NOT NULL,

    PRIMARY KEY (courseNumber, sectionNumber, term, name),

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term),

    FOREIGN KEY (courseNumber, sectionNumber, term, categoryName)
        REFERENCES Categories(courseNumber, sectionNumber, term, name)
);

CREATE TABLE Enrolled (
    studentID INT NOT NULL,
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,

    PRIMARY KEY (studentID, courseNumber, sectionNumber, term),

    FOREIGN KEY (studentID)
        REFERENCES Students(studentID),

    FOREIGN KEY (courseNumber, sectionNumber, term)
        REFERENCES Classes(courseNumber, sectionNumber, term)
);

CREATE TABLE Grades (
    studentID INT NOT NULL,
    courseNumber VARCHAR(20) NOT NULL,
    sectionNumber INT NOT NULL,
    term VARCHAR(30) NOT NULL,
    assignmentName VARCHAR(100) NOT NULL,
    points DECIMAL(8,2),

    PRIMARY KEY (
        studentID,
        courseNumber,
        sectionNumber,
        term,
        assignmentName
    ),

    FOREIGN KEY (studentID)
        REFERENCES Students(studentID),

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
        ),

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
);
