# Java-Based Attendance System

## Project Overview
This is a Java-based attendance system that uses OpenCV (via JavaCPP) for face detection and recognition to register students and mark their attendance. The system captures images from a webcam, registers students with their names and roll numbers, recognizes faces to mark attendance, and sends an attendance report via email as a CSV file.

## Features
- **Student Registration**: Captures 50 images per student for training a face recognition model.
- **Attendance Tracking**: Detects faces in real-time and marks students as present or absent.
- **Email Reporting**: Generates a CSV file with attendance data and sends it via email.
- **GUI**: Provides a simple Swing-based interface with buttons for registration, attendance, and sending reports.

## Prerequisites
- Java Development Kit (JDK) 8 or higher
- OpenCV (via JavaCPP) for face detection and recognition
- JavaMail API for sending emails
- A webcam for capturing images
- Maven or Gradle for dependency management (recommended)
- SMTP server credentials (e.g., Gmail SMTP)

## Directory Structure
```
attendance-system/
├── data/
│   ├── haarcascade_frontalface_alt2.xml
│   ├── studentData.csv
│   ├── attendanceSheet.csv
│   └── lbph_model.yml
├── images/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── opencvcamera/
│                   └── Camera.java
├── pom.xml
├── .gitignore
└── README.md
```
## Usage
1. Run the application:
   ```bash
   mvn clean install
   mvn exec:java -Dexec.mainClass="com.opencvcamera.Camera"
   ```
2. Use the GUI:
   - Click "Register Student" to capture images for a new student (enter name and roll number).
   - Click "Take Attendance" to start face recognition and mark attendance.
   - Click "Send Attendance" to generate a CSV report and send it via email.

## Notes
- Ensure the `data/` and `images/` directories exist before running the application.
- The face recognition model (`lbph_model.yml`) is saved in the `data/` directory.
- Replace placeholder email credentials in `Camera.java` with valid SMTP credentials.
- The system requires a stable internet connection for sending emails.
