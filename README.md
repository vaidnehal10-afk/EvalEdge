# 📚 Student Evaluation Slot Booking System (Java + JDBC)

## 🚀 Overview

This project is a **backend-based slot booking system** developed using **Java and JDBC**.
It allows teachers to create and manage evaluation time slots, and students to book available slots.

The system follows a **role-based approach**:

* 👨‍🏫 Teacher (Admin)
* 👨‍🎓 Student (User)

---

## 🎯 Features

### 👨‍🏫 Teacher (Admin)

* Register as new teacher or login as existing
* Add availability (day + time range)
* Automatic generation of **30-minute slots**
* Add multiple slots in one session
* Edit/Delete slots (optional extension)

---

### 👨‍🎓 Student (User)

* Register or login
* Select teacher
* View available slots (day-wise)
* Book a slot
* Prevent booking if:

  * No slots available
  * Slot already booked
  * Invalid slot ID

---

## 🛠️ Technologies Used

* Java
* JDBC (Java Database Connectivity)
* MySQL
* VS Code

---

## 📂 Project Structure

```
EvaluationSystem/
│
├── DBConnection.java
├── TeacherService.java
├── StudentService.java
├── BookingService.java
├── Main.java
├── lib/
│    └── mysql-connector-j-9.6.0.jar
```

---

## 🗄️ Database Setup

### Step 1: Create Database

```sql
CREATE DATABASE evaluation_db;
USE evaluation_db;
```

---

### Step 2: Create Tables

```sql
CREATE TABLE teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
);

CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
);

CREATE TABLE slots (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT,
    day VARCHAR(20),
    start_time TIME,
    end_time TIME,
    is_booked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    slot_id INT,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (slot_id) REFERENCES slots(slot_id)
);
```

---

## ⚙️ Setup Instructions

### 1️⃣ Install MySQL

Install and start MySQL server.

---

### 2️⃣ Download JDBC Driver

Download MySQL Connector/J and place it in:

```
lib/mysql-connector-j-9.6.0.jar
```

---

### 3️⃣ Configure VS Code

Create `.vscode/settings.json`:

```json
{
  "java.project.referencedLibraries": [
    "lib/**/*.jar"
  ]
}
```

---

### 4️⃣ Update Database Credentials

In `DBConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/evaluation_db";
private static final String USER = "root";
private static final String PASS = "your_password";(your pass word the one you use for MySql)
```

---

## ▶️ How to Run

### Compile:

```bash
javac *.java
OR
javac -cp ".;lib/*" *.java
```

### Run:

```bash
java Main
OR
java -cp ".;lib/*" Main
```

---

## 🧪 Sample Flow

### Teacher:

1. Select Teacher role
2. Register/Login
3. Enter day and time range
4. System generates slots

---

### Student:

1. Select Student role
2. Register/Login
3. Enter teacher ID and day
4. View available slots
5. Book slot

---

## ⚠️ Important Notes

* Time must be entered in **HH:MM format** (e.g., 12:00)
* Each slot is **30 minutes**
* A slot can be booked only once
* Foreign key constraints ensure data integrity

---

## 💡 Future Enhancements

* GUI (JavaFX / Swing)
* Authentication system
* Slot editing UI
* Email notifications
* Multi-slot booking limits


## ⭐ Conclusion

This project demonstrates:

* JDBC integration
* Database design with foreign keys
* Role-based access control
* Real-world scheduling logic

---
