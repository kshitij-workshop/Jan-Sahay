# Government Scheme Assistant

A citizen-first platform helping residents of Bihar/India discover government schemes, check eligibility, manage documents, and prepare applications.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 3.x, Spring Data JPA, Spring Security, Spring AI, MySQL, Maven |
| Frontend | React 18, Vite, TypeScript, React Router, Axios |
| Database | MySQL 8.0 with Flyway migrations |
| Infrastructure | Docker Compose |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local backend development)
- Node.js 20+ (for local frontend development)

### 1. Clone and Configure

```bash
git clone <repository-url>
cd government-scheme-assistant
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start with Docker Compose

```bash
docker-compose up -d
```

Services:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- MySQL: localhost:3306

### 3. Local Development

**Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Project Structure

```
government-scheme-assistant/
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Source code
│   ├── src/main/resources/ # Config, migrations
│   ├── src/test/           # Tests
│   └── pom.xml
├── frontend/               # React + Vite application
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docs/                   # Documentation
│   ├── architecture.md
│   ├── implementation-plan.md
│   ├── api.md
│   ├── database.md
│   ├── eligibility-engine.md
│   ├── document-engine.md
│   └── ai.md
├── docker-compose.yml
├── .env.example
└── README.md
```

## Core Features

1. **Scheme Discovery** - Browse/search government schemes from myScheme.gov.in
2. **Eligibility Engine** - Deterministic rule-based matching with explanations
3. **Document Vault** - Upload and manage documents (Aadhaar, certificates, etc.)
4. **Mismatch Detection** - Compare profile data with document extracts
5. **Smart Form Assistant** - Dynamic form preparation with profile auto-fill
6. **Notifications** - In-app + WhatsApp (mock for demo)
7. **AI Assistance** - Explanations, translations, form mapping help
8. **Admin Dashboard** - Scheme sync, monitoring, mapping approval

## Demo Mode

The application includes a complete demo mode that works without external credentials:

```bash
# In .env
USE_MOCK_OCR=true
USE_MOCK_WHATSAPP=true
MAX_SYNC_SCHEMES=50
```

Demo journey:
1. Register → Create profile (Bihar student, income, caste)
2. System calculates eligible schemes
3. View matches with explanations
4. Upload documents (Aadhaar, Income Certificate)
5. Mock OCR extracts fields
6. System detects intentional income mismatch
7. Notification created
8. Open Smart Form → auto-filled → review → "Open Official Portal"

## Configuration

Key environment variables (see `.env.example`):

| Variable | Required | Description |
|----------|----------|-------------|
| `MYSQL_*` | Yes | Database connection |
| `JWT_SECRET` | Yes | 64-char base64 secret |
| `MYSCHEME_API_KEY` | For sync | myScheme.gov.in API key |
| `SPRING_AI_OPENAI_API_KEY` | For AI | OpenAI API key |
| `WHATSAPP_*` | For WhatsApp | Meta Cloud API credentials |

## Documentation

- [Architecture](docs/architecture.md) - System design, data flows, modules
- [Implementation Plan](docs/implementation-plan.md) - Phase-by-phase roadmap
- [API](docs/api.md) - REST endpoints (to be created)
- [Database](docs/database.md) - Schema details (to be created)
- [Eligibility Engine](docs/eligibility-engine.md) - Rules, results (to be created)
- [Document Engine](docs/document-engine.md) - Extraction, mismatch (to be created)
- [AI Integration](docs/ai.md) - Spring AI usage, guardrails (to be created)

## Development Phases

See [Implementation Plan](docs/implementation-plan.md) for detailed phases:

- Phase 1: Foundation (Spring Boot, React, Docker, MySQL)
- Phase 2: Authentication (JWT, roles)
- Phase 3: User Profile
- Phase 4: MyScheme Integration & Sync
- Phase 5: Scheme Browsing
- Phase 6: Eligibility Engine
- Phase 7: User-Scheme Matching
- Phase 8: Document Vault
- Phase 9: Document Extraction (Mock)
- Phase 10: Document Mismatch Engine
- Phase 11: Notifications
- Phase 12: Smart Form Assistant
- Phase 13: Spring AI Integration
- Phase 14: Admin Dashboard
- Phase 15: Polish & Demo Readiness

## Security

- JWT-based authentication with refresh tokens
- BCrypt password hashing
- Role-based access control (USER, ADMIN)
- API keys never exposed to frontend
- No secrets in code or git
- Structured audit logging (no PII)

## License

MIT License - See LICENSE file for details.
<!-- churn-test 21 -->
