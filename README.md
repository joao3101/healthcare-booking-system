# Healthcare Booking System

A simple Spring Boot application for managing healthcare appointments.

---

## 🚀 How to Run Locally

### Prerequisites
- [Docker](https://www.docker.com/get-started) installed
- (Optional) [Gradle](https://gradle.org/install/) if you want to build locally without Docker

---

### Run with Docker

1. Build the Docker image:

    ```bash
    docker build -t healthcare-booking .
    ```

2. Run the container:

    ```bash
    docker run -p 8080:8080 healthcare-booking
    ```

3. Access the application at:

    ```
    http://localhost:8080
    ```

---

### Run Locally with Gradle

1. Build the JAR:

    ```bash
    ./gradlew bootJar
    ```

2. Run the application:

    ```bash
    java -jar build/libs/*.jar
    ```

---

## Some simple assumptions

1. The appointment window is in milliseconds since the epoch.

2. The appointment window is in the future.

3. I've preloaded the database with some doctors and rooms. The doctors specialties are Cardiology, Neurology and Pediatrics. For more information on some assumptions that I've made, please check the assumptions_and_simplifications.md file.

## API Endpoints

### Create Appointment

```http
POST /v1/appointments
Content-Type: application/json

{
  "patient_email": "john.doe@example.com",
  "patient_name": "John Doe",
  "start_date": 1693465200000,
  "end_date": 1693468800000,
  "specialty": "Cardiology"
}
```

---

### Get All Appointments

```http
GET /v1/appointments
```
