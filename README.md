# BearPoints - Student Recognition System

[![Java Version](https://img.shields.io/badge/java-24-blue.svg)]()
[![Spring Boot](https://img.shields.io/badge/spring_boot-3.5.0-green.svg)]()
[![React](https://img.shields.io/badge/react-19.0.0-blue.svg)]()
[![AWS](https://img.shields.io/badge/AWS-Live-orange.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

**BearPoints** is a production-grade student recognition and reward system for Buchanan Elementary School. Teachers and staff can recognize positive student behavior, track points, and manage rewards through role-based dashboards.

**Live Production:** [https://bearpoints.org](https://bearpoints.org)

---

## 🏗️ Production Architecture

```mermaid
flowchart TB
    subgraph "DNS & CDN"
        R53[Route 53<br/>bearpoints.org]
        CF[CloudFront CDN]
    end

    subgraph "Frontend Hosting"
        S3[S3 Bucket<br/>Static React App]
    end

    subgraph "API Layer"
        ALB[Application Load Balancer<br/>HTTPS Termination]

        subgraph "ECS Cluster (Fargate)"
            API1[API Task 1<br/>:8080]
            API2[API Task 2<br/>:8080]
            APIN[API Task N<br/>:8080]
        end
    end

    subgraph "Data Layer"
        RDS[(RDS PostgreSQL<br/>Primary Database)]
        SM[Secrets Manager<br/>Credentials]
    end

    subgraph "External Services"
        FB[Firebase Auth<br/>Google SSO]
        GS[Google Sheets API<br/>Data Sync]
    end

    %% Connections
    R53 --> CF
    CF --> S3
    CF --> ALB
    ALB --> API1 & API2 & APIN
    API1 & API2 & APIN --> RDS
    API1 & API2 & APIN --> SM
    API1 & API2 & APIN --> FB
    API1 & API2 & APIN --> GS
```

## AWS Services in Production

| Service | Purpose | Status |
|---------|---------|--------|
| **Route53** | DNS management for bearpoints.org | ✅ Live |
| **CloudFront** | CDN for gloabl low-latency delivery | ✅ Live |
| **S3** | Static React hosting | ✅ Live |
| **ACM** | SSL/TLS certificates | ✅ Live |
| **ALB** | Load balancing & HTTPS termination | ✅ Live |
| **ECS (Fargate)** | Container orchestration | ✅ Live |
| **ECR** | Docker image registry | ✅ Live |
| **RDS (PostgreSQL)** | Primary application database | ✅ Live |
| **Secrets Manager** | Secure credential storage | ✅ Live |
| **VPC** | Isolated network environment | ✅ Live |

---

## 📁 Repository Structure

```
BearPoints/
├── api/ # Spring Boot backend
│ ├── src/
│ │ ├── main/java/ # Application code
│ │ └── test/ # Unit & integration tests
│ ├── Dockerfile
│ └── pom.xml
├── ui/ # React frontend
│ ├── src/
│ │ ├── components/ # Reusable UI components
│ │ ├── hooks/ # Custom React hooks
│ │ ├── pages/ # Route pages
│ │ ├── services/ # API client
│ │ ├── store/ # Redux state
│ │ └── utils/ # Helpers
│ ├── Dockerfile
│ └── package.json
├── .github/workflows/
│ ├── test.yml # PR validation
│ └── deploy.yml # Production deployment
├── docker-compose.yml # Local development
├── DEPLOY.md
└── README.md
```

--- 

## 🚀 Live Deployment Access

| Environment | URL | Description |
|-------------|-----|-------------|
| **Production** | https://bearpoints.org | Live application |
| **API** | https://api.bearpoints.org | REST API endpoint |
| **API Docs** | https://api.bearpoints.org/swagger-ui.html | Interactive API documentation |
| **Health Check** | https://api.bearpoints.org/actuator/health | Service health status |

---

## 👥 Role-Based Access

| Role | Dashboard | Permissions |
|------|-----------|-------------|
| **Admin** | Full system control | Create/Edit/Delete all entities, user management, Google Sheets sync |
| **Staff** | Management views | Create/Edit/Delete all entities, user management, Google Sheets sync |
| **Teacher** | Classroom focus | Manage own students, submit brag logs, classroom leaderboard |
| **Para** | Support role | Submit brag logs, view students |
| **Student** | Personal view | View points, rewards, leaderboards |

---

## 🔐 Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant UI as React App
    participant FB as Firebase Auth
    participant API as Spring Boot API
    participant DB as PostgreSQL

    U->>UI: Click Login
    UI->>FB: Google Sign-In Popup
    FB->>U: Select OKCPS Account
    U->>FB: Confirm
    FB->>UI: ID Token
    UI->>API: GET /api/users/me (Bearer Token)
    API->>FB: Verify Token
    FB-->>API: Token Valid
    API->DB: Fetch User
    DB-->>API: User Data
    API-->>UI: User DTO + Role
    UI->>UI: Redirect to Role Dashboard
```

## 📊 Key Features

### For Teachers

- **Classroom Leaderboard** - Real-time points ranking with timeframe filters (Week/Month/Semester/Year)
- **Student Management** - Create, edit, and manage classroom rosters
- **Brag Logs** - Submit and track student recognition
- **QR Codes** - Print QR codes for quick brag submissions

### For Students

- **Points Dashboard** - View personal point total
- **Reward Store** - Redeem points for rewards
- **Achievement History** - Track brag logs and redeemed rewards
- **Classroom Leaderboard** - See ranking within class

### For Admins/Staff

- **User Management** - Create and manage ADMIN, STAFF, PARA users
- **Teacher Management** - Assign grade levels and manage teacher accounts
- **Behavior Types** - Configure point values and active status
- **Reward Items** - Manual inventory and point costs
- **System Sync** - Manual Google Sheets synchronization

### Data Synchronization

- **Automated Sync** - Runs daily at 8 AM and 8 PM
- **Manual Trigger** - Admin/Staff can sync on demand
- **Batch Processing** - 100 rows per batch with retry logic
- **Bidirectional** - Database ↔ Google Sheets

---

## 🛠️ Local Development

### Prerequisites

- Docker & Docker Compose
- Node.js 22+ (optional for local UI development)
- Java 24 JDK (optional for local API development)

### Quick Start with Docker Compose

```bash
# Clone the repository
git clone https://github.com/dmerc12/BearPoints.git
cd BearPoints

# Copy environment template
cp .env.example .env

# Edit .env with your configuration
# (Firebase keys, Google service account, etc.)

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### Services Available
| Service | URL | Description |
|---------|-----|-------------|
| Frontend (Dev) | http://localhost:5173 | React development server |
| API | http://localhost:8080 | Spring Boot application |
| Database | localhost:5432 | PostgreSQL |

### Environment Variables

Create a `.env` file with:

```bash
# Test administratior
TEST_EMAIL='example@okcps.org'

# Database
DB_USERNAME='bearpoints'
DB_PASSWORD='bearpoints'
DB_NAME='bearpoints'
DB_HOST=localhost
DB_PORT=5432

# Show SQL in logs
SHOW_SQL=true

# Applicaiton port
PORT=8080

# Google
GOOGLE_SERVICE_ACCOUNT_KEY={"type": "service_account", ...}
GOOGLE_SHEET_ID='your-google-sheet-id'

# Firebase
FIREBASE_SERVICE_ACCOUNT_KEY={"type": "service_account", ...}
FIREBASE_WEB_API_KEY="your-api-key"
FIREBASE_AUTH_DOMAIN='your-auth-domain'

# Frontend (build time)
VITE_API_URL='http://localhost:8080/api'
VITE_FIREBASE_API_KEY='your-api-key'
VITE_FIREBASE_AUTH_DOMAIN='your-auth-domain'
VITE_FIREBASE_PROJECT_ID='your-project-id'
VITE_FIREBASE_STORAGE_BUCKET='your-storage-bucket'
VITE_FIREBASE_MESSAGING_SENDER_ID='your-sender-id'
VITE_FIREBASE_APP_ID='your-app-id'
```

---

## 🧪 Testing

```bash
# Backend tests (unit + integration with Testcontainers)
cd api
./mvnw clean verify
```

---

## 🚢 CI/CD Pipeline

```mermaid
flowchart LR
    subgraph "GitHub Actions"
        P[Push → dev] --> Test[Run Tests]
        Test -->|Pass| PR[Pull request to main]
        PR --> Build[Build & Push]
    end

    subgraph "AWS"
        Build --> ECR[ECR Image]
        ECR --> ECS[ECS Service Update]
        Build --> S3[S3 Sync]
        S3 --> CF[CloudFront Invalidation]
    end

    style P fill:#f9f,stroke:#333
    style ECS fill:#9f9,stroke:#333
    style CF fill:#9f9,stroke:#333
```

### Deployment Triggers

| Branch | Trigger | Action |
|--------|---------|--------|
| `dev` | Push | Run tests only |
| `main` | Pull Request | Full Deployment (API + UI) |

---

## 📈 Monitoring & Health

### Health Endpoints

```bash
# API Health Check
curl https://api.bearpoints.org/actuator/health

## Expected response
{"status":"UP"}
```

### CloudWatch Monitoring
| Metric | Description | Alarm Threshold |
|--------|-------------|-----------------|
| CPUUtilization | ECS task CPU usage | >80% for 5 minutes |
| MemoryUtilization | ECS task memory usage | >85% for 5 minutes |
| DatabaseConnections | RDS connection count | >80% of max |
| HTTPCode_Target_5XX | ALB 5xx error rate | >1% for 5 minutes |

### Logging

| Log Source | Destination | Retention |
|------------|-------------|-----------|
| Application Logs | CloudWatch `/ecs/bearpoints-api` | 30 days |
| Access Logs | S3 (ALB logs) | 30 days |
| Database Logs | RDS Enhanced Monitoring | 7 days |

---

## 🔒 Security Compliance

| Control | Implementation |
|---------|----------------|
| Authentication | Firebase Auth with Google SSO |
| Authorization | Role-based access (5 roles) |
| Data in Transit | TLS 1.2+ (ACM + CloudFront + ALB) |
| Data at Rest | RDS encryption, Secrets Manager |
| Network Isolation | VPC with private subnets |
| Credential Management | AWS Secrets Manager |
| CORS | Restricted to [bearpoints.org](bearpoints.org) domains |
| Input Validation | Spring Validation + JPA constraints |
| SQL Injection | JPA/Hibernate parameterized queries |
| XSS Protection | React escaping + CSP headers |

---

## 📚 Documentation

| Document | Location | Description |
|----------|----------|-------------|
| API Documentaiton | `/api/README.md` | Backend setup and configuration |
| Frontend Documentation | `ui/README.md` | UI development and deployment |
| Deployment Guide | `DEPLOYMENT.md` | AWS infrastructure setup |
| API Swagger UI | [https://api.bearpoints.org/swagger-ui.html](https://api.bearpoints.org/swagger-ui.html) | Interactive API docs |

---

## 🗄️ Database Schema

```mermaid
erDiagram
    User ||--o| Teacher : "has"
    User ||--o| Student: "has"
    Teacher ||--o{ Student : "teaches"
    Teacher ||--o{ BragLog : "submits"
    Student ||--o{ BragLog : "receives"
    Student ||--o{ StudentReward : "redeems"
    RewardItem ||--o{ StudentReward : "is"
    BragLog }o--o{ BehaviorType : "includes"

    User {
        bigint id PK
        string email UK
        string first_name
        string last_name
        enum role
        boolean active
    }

    Teacher {
        bigint id PK
        bigint user_id FK
        enum grade
        boolean active
    }

    Student {
        bigint id PK
        bigint user_id FK
        bigint teacher_id FK
        int points
        string token UK
        boolean active
    }

    BehaviorType {
        bigint id PK
        string name UK
        int point_value
        boolean active
    }

    BragLog {
        bigint id PK
        bigint student_id FK
        bigint teacher_id FK
        int points_generated
        string submitter_name
        text notes
        timestamp created_at
    }

    RewardItem {
        bigint id PK
        string name UK
        int point_cost
        int stock
        boolean active
    }

    StudentReward {
        bigint id PK
        bigint student_id FK
        bigint reward_item_id FK
        timestamp redeemed_at
    }
```

---

## 🔄 Google Sheets Sync Process

```mermaid
flowchart TD
    A[Scheduled: 8AM/8PM<br/>or Manual Trigger] --> B{Check Daily Quota}
    B -->|Under 80 percent| C[Fetch Unsynced Entities]
    B -->|Over 80 percent| D[Skip Sync & Log Warning]

    C --> E{Entity Type}

    E -->|Users| F1[Sync Users Sheet]
    E -->|Teachers| F2[Sync Teachers Sheet]
    E -->|Students| F3[Sync Students Sheet]
    E -->|Behavior Types| F4[Sync BehaviorTypes Sheet]
    E -->|Brag Logs| F5[Sync BragLogs Sheet]
    E -->|Reward Items| F6[Sync RewardItems Sheet]
    E -->|Student Rewards| F7[Sync StudentRewards Sheet]

    F1 & F2 & F3 & F4 & F5 & F6 & F7 --> G{Batch Size > 100?}

    G -->|Yes| H[Split into 100-row chunks]
    G -->|No| I[Execute Batch Update]
    H --> I

    I --> J{API Error?}
    J -->|429 Quota| K[Exponential Backoff Retry]
    J -->|Other Error| L[Log Error & Continue]
    J -->|Success| M[Mark Entities as Synced]

    K -->|Max Retries| L
    K -->|Success| M

    M --> N[Update lastSynced Timestamp]
    N --> O[Sync Complete]
```

---

## 📝 Release Notes

### Current Production Version: v1.0.0

#### Features:

- ✅ Role-based authentication (5 roles)
- ✅ QR code generation for students
- ✅ Public brag submission via QR scann
- ✅ Classroom leaderboards with timeframes
- ✅ Reward redemption system
- ✅ Google Sheets bidirectional sync
- ✅ Responsive Bootstrap UI

#### Infrastructure:

- ✅ Multi-AZ deployment
- ✅ Auto-scaling ECS tasks
- ✅ SSL/TLS everywhere
- ✅ CloudFront CDN
- ✅ Secrets Manager integration
- ✅ CI/CD via GitHub Actions

---

## 👥 Team

- **Dylan Mercer** - Lead Developer

---

## 📄 License

MIT License - See LICENSE file for details

---

## 🆘 Support

For issues or questions:
- Create a GitHub issue
- Contact the development team

---

**Built with ☕ and 🐻 at Buchanan Elementary**
