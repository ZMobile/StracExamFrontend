# **StracExamFrontend**

## **Overview**

StracExamFrontend is a Java Swing application that serves as the user interface for the StracExam project. It integrates with the backend (`StracExamBackend`) to provide the following core functionalities:

- **User Authentication**: Redirect users to authenticate with Google OAuth 2.0.
- **File Management**:
    - Display a list of files from the user's Google Drive with metadata (name, type, last modified date).
    - Upload files from the local system to Google Drive.
    - Download files from Google Drive to the local system.
    - Delete files from Google Drive.

This application is designed to be lightweight, experimental, and easily modifiable to accommodate future enhancements. As such, no unit tests or integration tests are provided in this repository.

---

## **Features**

- **Google Drive Integration**:
    - Seamless integration with the backend to manage files on Google Drive.
    - Intuitive and responsive Java Swing interface.
- **Experimental Design**:
    - The frontend is intentionally left flexible for quick prototyping and modifications.

---

## **Prerequisites**

To run the application, ensure you have the following:

1. **Java**: Version 21 or higher.
2. **StracExamBackend**:
    - Ensure the backend is running, as the frontend depends on its APIs.

---

## **Setup Instructions**

### **1. Clone the Repository**
```bash
git clone https://github.com/your-username/StracExamFrontend.git
cd StracExamFrontend
```

### **2. Compile the Application**
To compile the application, run the following command:
```bash
mvn clean install
```

### **3. Configure the BACKEND_BASE_URL**
In the `Main.java` file, set the `BACKEND_BASE_URL` to the URL where the backend is running. Set to "http://localhost:8080" by default.

### **3. Run the Application**

To start the application, execute the following:
```bash
java -cp target/StracExamFrontend.jar org.strac.frontend.Main
