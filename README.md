# ln-foot

## Description

ln-foot is a Spring Boot application.

## Technologies Used

*   Java
*   Spring Boot
*   PostgreSQL

## Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Gradle
*   PostgreSQL database

## Setup Instructions

1.  **Clone the repository:**

    ```bash
    git clone <repository_url>
    ```

2.  **Configure the database:**

    *   Ensure that you have a PostgreSQL database running.
    *   Update the `src/main/resources/application.properties` file with your database credentials:

        ```properties
        spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
        spring.datasource.username=<username>
        spring.datasource.password=<password>
        ```

3.  **Build the application:**
    Using Gradle:

    ```bash
    gradle clean build
    ```

4.  **Run the application:**

    Using Gradle:

    ```bash
    gradle bootRun
    ```

## Configuration

The application is configured using the `src/main/resources/application.properties` file.  You can override these properties using environment variables or command-line arguments.

### Security

The application uses basic authentication. The default username and password are:

*   Username: `admin`
*   Password: `admin123`

**Warning:** It is highly recommended to change these default credentials in a production environment.