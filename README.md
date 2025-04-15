# LN Foot API

A Spring Boot application with Keycloak integration for authentication and authorization.

## Prerequisites

- Docker and Docker Compose
- Java 17
- Gradle
- Make
- Google Cloud Console account (for OAuth credentials)

## Setup

1. **Google OAuth Setup**
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Create a new project
   - Enable Google OAuth API
   - Create OAuth 2.0 credentials
   - Add authorized redirect URIs:
     - `http://localhost:8180/realms/ln-foot-01/broker/google/endpoint`
     - `http://localhost:8180/realms/ln-foot-01/broker/google/endpoint/login`

2. **Environment Configuration**
   - Copy `.env.example` to `.env`
   - Update the following variables:
     ```
     GOOGLE_CLIENT_ID=your-google-client-id
     GOOGLE_CLIENT_SECRET=your-google-client-secret
     KEYCLOAK_ADMIN=admin
     KEYCLOAK_ADMIN_PASSWORD=your-secure-password
     ```

## Running the Application

### Using Make (Recommended)

The project includes a Makefile for common operations:

```bash
# Start all services (Keycloak + Spring Boot)
make dev

# Build the application
make build

# Run tests
make test

# Clean everything
make clean-all

# Show all available commands
make help
```

### Manual Steps

1. **Start Keycloak**
   ```bash
   docker compose up -d
   ```
   - Wait for Keycloak to start (usually takes 30-60 seconds)
   - Access Keycloak admin console at `http://localhost:8180`
   - Login with admin credentials from `.env`

2. **Start Spring Boot Application**
   ```bash
   ./gradlew bootRun
   ```
   - The application will start on port 8080
   - Access Swagger UI at `http://localhost:8080/swagger-ui/index.html`

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Security

- All endpoints are public by default
- Protected endpoints are secured with `@PreAuthorize` annotations
- Authentication is handled by Keycloak with Google OAuth
- JWT tokens are used for API authentication
- Two roles are available:
  - `admin`: Full access to all endpoints
  - `user`: Limited access to specific endpoints

## Development

- The application uses Spring Boot 3.4.4
- Keycloak is configured with Google as the identity provider
- PostgreSQL is used as the database
- Swagger/OpenAPI for API documentation

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── co/hublots/ln_foot/
│   │   │       ├── config/
│   │   │       ├── controllers/
│   │   │       ├── models/
│   │   │       └── services/
│   │   └── resources/
│   └── test/
├── .docker/
│   └── keycloak-config/
├── Dockerfile
├── Makefile
├── compose.yml
└── build.gradle
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request