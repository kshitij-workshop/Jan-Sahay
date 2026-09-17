# Architecture Document - Government Scheme Assistant

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         GOVERNMENT SCHEME ASSISTANT                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │   React      │    │  Spring Boot │    │   MySQL      │              │
│  │   Frontend   │◄───│   Backend    │◄───│   Database   │              │
│  │   (Vite)     │    │   (Java)     │    │              │              │
│  └──────────────┘    └──────┬───────┘    └──────────────┘              │
│                             │                                           │
│                    ┌────────┴────────┐                                  │
│                    │  External APIs  │                                  │
│                    │  • myScheme.gov │                                  │
│                    │  • LLM Provider │                                  │
│                    │  • WhatsApp     │                                  │
│                    │  • OCR Provider │                                  │
│                    └─────────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Backend Architecture

### Module Structure (Domain-Driven)

```
backend/src/main/java/com/govscheme/
├── auth/                    # Authentication & Authorization
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── security/
│   └── entity/
├── user/                    # User management
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── entity/
├── profile/                 # Citizen profile (extensible)
│   ├── entity/
│   ├── service/
│   ├── dto/
│   └── completeness/
├── scheme/                  # Government schemes
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── dto/
│   ├── client/              # MyScheme API client
│   ├── sync/                # Ingestion pipeline
│   └── controller/
├── eligibility/             # Deterministic eligibility engine
│   ├── rule/                # Individual rules (AgeRule, IncomeRule, etc.)
│   ├── service/
│   ├── dto/
│   └── engine/
├── document/                # Document vault & processing
│   ├── entity/
│   ├── service/
│   ├── storage/             # Storage abstraction
│   ├── extraction/          # OCR abstraction
│   ├── mismatch/            # Comparison engine
│   └── controller/
├── matching/                # User-scheme matching
│   ├── entity/
│   ├── service/
│   └── scheduler/
├── application/             # Smart Form Assistant
│   ├── entity/
│   ├── service/
│   ├── dto/
│   └── controller/
├── notification/            # Notification system
│   ├── entity/
│   ├── service/
│   ├── channel/             # Provider abstraction
│   └── controller/
├── ai/                      # Spring AI integration
│   ├── service/
│   ├── prompt/
│   └── tool/
├── admin/                   # Admin operations
│   ├── controller/
│   └── service/
└── common/                  # Shared utilities
    ├── exception/
    ├── dto/
    ├── config/
    └── util/
```

### Key Design Principles

1. **Deterministic Eligibility**: Rules are pure Java, no LLM decisions
2. **Provider Abstractions**: MySchemeClient, DocumentStorageService, DocumentExtractionService, NotificationProvider, WhatsAppProvider
3. **Thin Controllers**: Business logic in services
4. **DTOs at Boundaries**: Never expose JPA entities
5. **Raw Data Preservation**: Store original myScheme JSON
6. **Interface Segregation**: Each external integration behind interface

---

## Database Schema (High-Level)

### Core Tables

| Table | Purpose |
|-------|---------|
| `users` | Authentication |
| `user_profiles` | Personal, address, education, economic, social, employment, family |
| `user_addresses` | Multiple addresses |
| `user_documents` | Document metadata + storage reference |
| `document_extracted_fields` | Normalized OCR output |
| `document_mismatches` | Profile vs document comparison results |
| `schemes` | Normalized scheme data |
| `scheme_eligibility` | Structured eligibility criteria |
| `scheme_documents` | Required documents per scheme |
| `scheme_faqs` | Frequently asked questions |
| `scheme_application_process` | Application steps |
| `scheme_categories/tags/states` | Lookup tables |
| `scheme_raw_data` | Original myScheme JSON |
| `user_scheme_matches` | Eligibility results per user-scheme |
| `notifications` | In-app + channel notifications |
| `form_templates` | Dynamic form definitions |
| `form_fields` | Form field definitions with profile mappings |
| `application_drafts` | User-prepared applications |
| `sync_jobs` | Sync execution tracking |
| `sync_errors` | Failed sync records |
| `audit_logs` | Important events |

### Key Indexes

- `user_profiles(user_id)`, `user_profiles(state, district)`
- `schemes(state, category)`, `schemes(slug)` unique
- `user_scheme_matches(user_id, scheme_id)` unique
- `user_documents(user_id, document_type)`
- `document_mismatches(document_id, severity)`

---

## Data Flow: Scheme Synchronization

```
myScheme API (paginated)
         │
         ▼
┌───────────────────────┐
│ SchemeSyncService     │
│ • Fetch search pages  │
│ • For each scheme:    │
│   - GET detail        │
│   - GET FAQs          │
│   - GET documents     │
│ • Normalize           │
│ • Store raw + normalized
│ • Track in sync_jobs  │
└───────────┬───────────┘
            │
            ▼
    MySQL Database
    (schemes + raw_data)
```

- Controlled concurrency (semaphore)
- Retry with exponential backoff
- 429/5xx handling
- Resume capability via `sync_jobs` cursor

---

## Data Flow: Eligibility Evaluation

```
User Profile + Scheme Eligibility Criteria
                │
                ▼
┌─────────────────────────────────────┐
│ EligibilityService                  │
│ ┌─────────────────────────────────┐ │
│ │ Rule Chain                      │ │
│ │ AgeRule      → RuleResult       │ │
│ │ StateRule    → RuleResult       │ │
│ │ IncomeRule   → RuleResult       │ │
│ │ CasteRule    → RuleResult       │ │
│ │ ...          → RuleResult       │ │
│ └─────────────────────────────────┘ │
│         │                           │
│         ▼                           │
│ Aggregate Results                   │
│ • Overall: ELIGIBLE/NOT_ELIGIBLE/   │
│   INSUFFICIENT_INFORMATION          │
│ • Missing fields list               │
│ • Per-rule explanations             │
└───────────┬─────────────────────────┘
            │
            ▼
    UserSchemeMatch (persisted)
    Notification (if new match)
```

### RuleResult Structure

```json
{
  "rule": "INCOME",
  "status": "PASS|FAIL|MISSING",
  "userValue": 150000,
  "requiredValue": "<=200000",
  "message": "Annual income ₹1,50,000 is within limit ₹2,00,000",
  "missingField": null
}
```

---

## Data Flow: Document Mismatch Detection

```
User Profile + Document Extracted Fields
                │
                ▼
┌─────────────────────────────────────┐
│ Normalization                       │
│ • Names: lowercase, trim, unicode   │
│ • Dates: YYYY-MM-DD                 │
│ • Gender: MALE/FEMALE/OTHER         │
│ • Address: fuzzy                    │
└───────────┬─────────────────────────┘
            │
            ▼
┌─────────────────────────────────────┐
│ Comparison Engine                   │
│ Field-by-field comparison           │
│ • Exact → MATCH                     │
│ • Minor diff → PROBABLE_MATCH       │
│ • Significant diff → MISMATCH       │
│ • No data → UNKNOWN                 │
└───────────┬─────────────────────────┘
            │
            ▼
    Severity Assignment
    • DOB mismatch → CRITICAL
    • Name major diff → CRITICAL
    • Address minor → WARNING
    • Formatting only → INFO
            │
            ▼
    DocumentMismatch records + Notification
```

---

## Data Flow: Smart Form Preparation

```
User selects "Apply with Smart Form"
                │
                ▼
┌─────────────────────────────────────┐
│ ApplicationDraftService             │
│ 1. Load FormTemplate for scheme     │
│ 2. Load UserProfile                 │
│ 3. For each FormField:              │
│    - Check profileField mapping     │
│    - If exists → prefill            │
│    - If missing → mark required     │
│ 4. Create ApplicationDraft          │
└───────────┬─────────────────────────┘
            │
            ▼
    Dynamic Form UI (React)
    • Prefilled values (editable)
    • Missing fields highlighted
    • Warnings from mismatches
    • Save draft / Review / Download
    • "Open Official Portal" button
```

---

## API Layer

### Authentication
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
```

### Profile
```
GET  /api/profile
PUT  /api/profile
```

### Schemes
```
GET  /api/schemes?page=&size=&category=&state=&gender=&age=...
GET  /api/schemes/{id}
GET  /api/schemes/{id}/eligibility
GET  /api/schemes/{id}/documents
GET  /api/schemes/{id}/faqs
```

### Matches
```
GET  /api/matches
GET  /api/matches/{schemeId}
```

### Documents
```
POST /api/documents          (multipart)
GET  /api/documents
GET  /api/documents/{id}
DELETE /api/documents/{id}
POST /api/documents/{id}/verify
GET  /api/documents/{id}/mismatches
```

### Readiness
```
GET /api/schemes/{id}/readiness
```

### Applications
```
POST /api/applications/drafts
GET  /api/applications/drafts
GET  /api/applications/drafts/{id}
PUT  /api/applications/drafts/{id}
```

### Notifications
```
GET /api/notifications
PUT /api/notifications/{id}/read
```

### Admin
```
POST /api/admin/schemes/sync
GET  /api/admin/sync/jobs
GET  /api/admin/sync/errors
GET  /api/admin/mappings
PUT  /api/admin/mappings/{id}/approve
```

---

## Security Architecture

```
┌────────────────────────────────────────┐
│           Spring Security              │
├────────────────────────────────────────┤
│  • JWT Access Token (15 min)           │
│  • Refresh Token (7 days, httpOnly)    │
│  • BCrypt (strength 12)                │
│  • Role-based: USER, ADMIN             │
│  • Method-level @PreAuthorize          │
│  • CORS configured for frontend origin │
└────────────────────────────────────────┘
```

- No secrets in code (env vars only)
- API keys never exposed to frontend
- Audit logging for sensitive operations

---

## AI Integration (Spring AI)

### Use Cases (Approved)
| Use Case | Service | Input | Output |
|----------|---------|-------|--------|
| Eligibility Explanation | EligibilityExplanationService | Structured RuleResults | Hindi/English explanation |
| Document Mismatch Explanation | DocumentMismatchExplanationService | Mismatch records | User-friendly warning |
| Scheme Q&A | SchemeQAService | User question + context | Answer with citations |
| Form Mapping Assistance | FormMappingAssistanceService | Form field label | Suggested profile mapping |

### Guardrails
- AI never decides eligibility (deterministic engine owns this)
- AI never validates documents (mismatch engine owns this)
- AI responses reference backend structured data
- Admin approves AI-suggested form mappings
- Tool/function calling for data access (no direct DB)

---

## Frontend Architecture

```
frontend/src/
├── components/          # Reusable UI components
│   ├── common/          # Button, Input, Card, Modal, etc.
│   ├── scheme/          # SchemeCard, SchemeDetail, SchemeFilters
│   ├── document/        # DocumentUpload, DocumentList, MismatchBanner
│   ├── application/     # DynamicForm, FieldRenderer, DraftActions
│   ├── notification/    # NotificationBell, NotificationList
│   └── profile/         # ProfileForm, CompletenessIndicator
├── pages/               # Route-level components
│   ├── Dashboard.jsx
│   ├── Schemes.jsx
│   ├── SchemeDetail.jsx
│   ├── MyMatches.jsx
│   ├── Documents.jsx
│   ├── Applications.jsx
│   ├── Notifications.jsx
│   ├── Profile.jsx
│   ├── Login.jsx
│   ├── Register.jsx
│   └── admin/
├── layouts/             # MainLayout, AuthLayout, AdminLayout
├── services/            # API clients (axios instances)
│   ├── api.js
│   ├── auth.js
│   ├── schemes.js
│   ├── documents.js
│   └── applications.js
├── hooks/               # Custom React hooks
│   ├── useAuth.js
│   ├── useSchemes.js
│   └── useDocuments.js
├── context/             # React Context providers
│   ├── AuthContext.jsx
│   └── NotificationContext.jsx
└── utils/               # Helpers, formatters, validators
```

---

## Docker Compose Services

```yaml
services:
  mysql:
    image: mysql:8.0
    environment: (from .env)
    volumes: mysql_data:/var/lib/mysql
    healthcheck: mysqladmin ping

  backend:
    build: ./backend
    ports: ["8080:8080"]
    environment: (from .env)
    depends_on: mysql (healthy)
    profiles: ["dev", "prod"]

  frontend:
    build: ./frontend
    ports: ["5173:5173"]  # Vite dev server
    environment: (from .env)
    profiles: ["dev"]

  # Production: frontend built into nginx, served by backend or separate
```

---

## Configuration Management

### Environment Variables (.env.example)
```bash
# Database
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=gov_scheme
MYSQL_USERNAME=app_user
MYSQL_PASSWORD=

# JWT
JWT_SECRET=
JWT_ACCESS_TOKEN_EXPIRY=900000
JWT_REFRESH_TOKEN_EXPIRY=604800000

# MyScheme API
MYSCHEME_API_KEY=
MYSCHEME_BASE_URL=https://api.myscheme.gov.in

# AI Provider
SPRING_AI_OPENAI_API_KEY=
SPRING_AI_MODEL=gpt-4o-mini

# WhatsApp (Meta Cloud API)
WHATSAPP_ACCESS_TOKEN=
WHATSAPP_PHONE_NUMBER_ID=
WHATSAPP_BUSINESS_ACCOUNT_ID=

# Storage
DOCUMENT_STORAGE_TYPE=local
LOCAL_STORAGE_PATH=./uploads

# App
SPRING_PROFILES_ACTIVE=dev
FRONTEND_URL=http://localhost:5173
```

---

## Observability

### Structured Logging (JSON)
```json
{
  "timestamp": "2026-09-17T10:30:00Z",
  "level": "INFO",
  "event": "ELIGIBILITY_CALCULATED",
  "userId": "uuid",
  "schemeId": "uuid",
  "result": "ELIGIBLE",
  "missingFields": []
}
```

### Key Events to Log
- USER_REGISTERED, PROFILE_UPDATED
- SCHEME_SYNC_STARTED/COMPLETED/FAILED
- ELIGIBILITY_CALCULATED
- DOCUMENT_UPLOADED/VERIFIED
- DOCUMENT_MISMATCH_DETECTED
- APPLICATION_DRAFT_CREATED
- NOTIFICATION_CREATED

### Never Log
- Passwords, JWT tokens, API keys
- Full document content (PII)

---

## Testing Strategy

### Unit Tests (Target: >80% on business logic)
- Eligibility rules (each rule in isolation)
- Eligibility engine aggregation
- Normalization utilities
- Document comparison
- Form mapping logic
- Notification aggregation

### Integration Tests
- Auth flow (register → login → access protected)
- Profile CRUD
- Scheme search/filter/detail
- Document upload → extraction → mismatch
- Application draft lifecycle
- Admin sync trigger

### Test Data
- Seed scripts for: admin, demo user, 20 schemes, documents, mismatches
- Synthetic data only (no real PII)

---

## Deployment Considerations

### Development
- `docker-compose up` spins up everything
- Hot reload: Spring Boot DevTools + Vite HMR
- Mock providers for WhatsApp, OCR

### Production Readiness
- Flyway migrations (versioned)
- Health checks (liveness/readiness)
- Connection pooling (HikariCP)
- Rate limiting on sync
- Secrets via Docker secrets / K8s secrets
- TLS termination at reverse proxy

---

## Future Extensibility

| Area | Extension Point |
|------|-----------------|
| New eligibility criteria | Add new `EligibilityRule` implementation |
| New document type | Add document type enum + extraction mapping |
| New notification channel | Implement `NotificationProvider` |
| New OCR provider | Implement `DocumentExtractionService` |
| New storage backend | Implement `DocumentStorageService` |
| New AI provider | Spring AI provider config |
| New scheme source | Add new `SchemeClient` implementation |

---

## Demo Mode

All external dependencies have mock implementations:
- `MockDocumentExtractionService` → returns predefined extracted data
- `MockWhatsAppProvider` → logs to console
- `LocalDocumentStorageService` → filesystem
- Seed data script for instant demo journey

This allows full demo without API keys or paid services.
<!-- churn-test 27 -->
