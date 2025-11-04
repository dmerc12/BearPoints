# Bear Points API - Backend Service
[![Java Version](https://img.shields.io/badge/java-24-blue.svg)]()
[![Spring Boot](https://img.shields.io/badge/spring_boot-3.5.0-green.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

The backend service for the Bear Points application, providing REST API endpoints for:
- 🔒 Firebase authentication and authorization
- 📝 Brag log management
- 🏆 Leaderboard calculations
- 📊 Google Sheets synchronization
- 🎁 Student reward tracking

## 🚀 Getting Started

### Prerequisites
- Java 24 JDK
- PostgreSQL 14+
- Maven 3.8+
- Google Cloud service account
- Firebase project

### Installation
1. **Navigate to backend directory:** 
   ```bash
   cd BearPoints/api
   ```
2. **Configure environment variables:**
   ```bash
   cp example.env.txt .env
   ```
   Edit the `.env` file with your actual configuration values:
   - For service account keys: Paste the **exact JSON content** without quotes
   - Example format: `{"type": "service_account", "project_id": ... }`
3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Database Setup
1. Create a PostgreSQL database matching your `DB_NAME`
2. The application will automatically create schema on first run via JPA

### 🔧Configuration
Configuration is managed through environment variables in `.env`:
**Important Service Account Formatting:**
- Paste JSON content directly without quotes
- Ensure no extra spaces before/after JSON
- Example: `GOOGLE_SERVICE_ACCOUNT_KEY={"type": "service_account", ...}`

| **Environment Variable**        | **Description**                              | **Default** |
|---------------------------------|----------------------------------------------|-------------|
| `DB_HOST`                       | PostgreSQL host                              | `localhost` |
| `DB_PORT`                       | PostgreSQL port                              | `5432`      |
| `DB_NAME`                       | Database name                                | -           |
| `DB_USERNAME`                   | Database username                            | -           |
| `DB_PASSWORD`                   | Database password                            | -           |
| `GOOGLE_SERVICE_ACCOUNT_KEY`    | **Content** of Google service account JSON   | -           |
| `GOOGLE_SHEET_ID`               | Google Sheet ID for synchronization          | -           |
| `FIREBASE_SERVICE_ACCOUNT_KEY`  | **Content** of Firebase service account JSON | -           |
| `PORT`                          | Server Port                                  | `8080`      |
| `SHOW_SQL`                      | Show SQL queries in logs                     | `true`      |

### API Documentation
Interactive API documentation is available at runtime:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

### Testing
Run tests with:
```bash
./mvnw test
```

Test coverage reports are generated in `target/site/jacoco/index.html`

### 🚦Health Monitoring
Application health endpoints:
- **Health Check**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`

### 🛡️Security Best Practices
1. **Environment management:**
   - Always keep `.env` in `.gitignore`
   - Use secrets management in production (Vault, AWS Secrets Manager)
2. **Service account keys:**
   - Store keys securely and rotate regularly
   - Limit permissions to only required services
   - **Formatting:** Paste JSON content directly without quotes or encapsulation
   - **Validation:** Ensure keys contain valid JSON without syntax errors
3. **Database security:**
   - Use network policies to restrict access
   - Enable SSL connections
   - Regular backups

### 📂Project Structure
```
api/
├── src/
│   ├── main/               # Application code
│   │   ├── java/com/bearpoints/api/
│   │   │   ├── config/     # Configuration classes
│   │   │   ├── controller/ # REST controllers
│   │   │   ├── dao/        # Data repositories
│   │   │   ├── dto/        # Data transfer objects
│   │   │   ├── entity/     # JPA entities
│   │   │   ├── exception/  # Exceptions and handlers
│   │   │   ├── security/   # Security configuration
│   │   │   ├── service/    # Business logic
│   │   │   └── ApiApplication.java # Main class
│   │   └── resources/      # Resources
│   └── test/               # Tests
├── .env                    # Environment variables (IGNORED)
├── example.env.txt         # Environment template
├── pom.xml                 # Maven configuration
└── README.md               # This file
```
**Configuration Notes:**
- `example.env.txt` shows required variables and their format
- Service account values should contain raw JSON content
