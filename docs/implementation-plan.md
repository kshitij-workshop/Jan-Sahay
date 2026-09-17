# Implementation Plan - Government Scheme Assistant

## Phase 0: Repository Analysis (COMPLETED)
- [x] Inspected repository - empty directory
- [x] No existing codebase
- [x] No git repository initialized
- [x] Technology stack confirmed: Spring Boot (Java), React (Vite), MySQL

---

## Phase 1: Project Foundation
**Goal**: Spring Boot + React + MySQL + Docker Compose running locally

### Backend Setup
- [ ] Spring Boot 3.x project with Maven
- [ ] Dependencies: Spring Web, Spring Data JPA, Spring Security, Spring AI, MySQL Driver, Flyway, JWT (jjwt), Validation
- [ ] Application.yml with profiles (dev, prod, test)
- [ ] Health endpoint (/actuator/health)
- [ ] Global exception handler
- [ ] Docker Compose with MySQL

### Frontend Setup
- [ ] React + Vite + TypeScript
- [ ] React Router, Axios
- [ ] Basic layout with header, sidebar
- [ ] Proxy configuration for API calls

### Infrastructure
- [ ] docker-compose.yml (MySQL, backend, frontend)
- [ ] .env.example with all placeholders
- [ ] README.md with setup instructions
- [ ] Git initialization

**Verification**: `docker-compose up` starts all services, health endpoints respond

---

## Phase 2: Authentication
**Goal**: Secure JWT-based auth with USER/ADMIN roles

### Backend
- [ ] User entity, UserRepository
- [ ] AuthController: register, login, refresh
- [ ] JWT token generation/validation
- [ ] Spring Security configuration
- [ ] BCrypt password encoding
- [ ] Role-based authorization (USER, ADMIN)

### Frontend
- [ ] Login/Register pages
- [ ] Auth context + token management
- [ ] Protected routes
- [ ] Axios interceptor for JWT

### Tests
- [ ] AuthController integration tests
- [ ] Security config tests

---

## Phase 3: User Profile
**Goal**: Complete citizen profile management

### Backend
- [ ] Profile entities: UserProfile, UserAddress, Education, Economic, Social, Employment, Family
- [ ] ProfileController: GET/PUT /api/profile
- [ ] Validation (Bean Validation)
- [ ] Profile completeness calculation

### Frontend
- [ ] Profile page with sections
- [ ] Form validation
- [ ] Progress indicator

---

## Phase 4: MyScheme Integration & Sync
**Goal**: Fetch, normalize, store government schemes

### Backend
- [ ] MySchemeClient (WebClient) with API key from env
- [ ] Scheme entities: Scheme, SchemeEligibility, SchemeDocuments, SchemeFAQs, SchemeApplicationProcess, SchemeCategory, SchemeTags, SchemeState
- [ ] Raw JSON storage (scheme_raw_data table)
- [ ] SyncService: paginated fetch → detail fetch → FAQs → documents → normalize → store
- [ ] Admin endpoint: POST /api/admin/schemes/sync
- [ ] Sync job tracking (sync_jobs, sync_errors tables)
- [ ] Retry logic, 429/5xx handling, controlled concurrency
- [ ] Development limit: maxSchemes config

### Frontend
- [ ] Admin sync trigger button
- [ ] Sync status display

---

## Phase 5: Scheme Browsing
**Goal**: Citizen-facing scheme discovery

### Backend
- [ ] SchemeController: search, filters, pagination, detail, FAQs, documents
- [ ] Search by: category, state, gender, age, student, occupation, caste, disability
- [ ] Scheme detail DTO with all sections

### Frontend
- [ ] Schemes listing page with filters
- [ ] Scheme detail page (overview, benefits, eligibility, documents, process, FAQs, official link)
- [ ] Scheme card component

---

## Phase 6: Eligibility Engine
**Goal**: Deterministic rule-based eligibility with explanations

### Backend
- [ ] Rule interface: `EligibilityRule` with `evaluate(profile, scheme)`
- [ ] Rules: AgeRule, StateRule, GenderRule, IncomeRule, CasteRule, OccupationRule, StudentRule, DisabilityRule, ResidenceRule, EmploymentRule, BplRule, MaritalStatusRule, MinorityRule
- [ ] EligibilityService orchestrating rules
- [ ] Result: ELIGIBLE / NOT_ELIGIBLE / INSUFFICIENT_INFORMATION
- [ ] Missing fields identification
- [ ] Structured rule results with pass/fail/missing + messages

### Tests (Critical)
- [ ] Age 22, req 18-70 → PASS
- [ ] Age 17, req 18-70 → FAIL
- [ ] Missing age → INSUFFICIENT_INFORMATION
- [ ] Bihar user + Bihar scheme → PASS
- [ ] Other state + Bihar-only → FAIL
- [ ] Missing income → INSUFFICIENT_INFORMATION

---

## Phase 7: User-Scheme Matching
**Goal**: Store and recalculate matches

### Backend
- [ ] UserSchemeMatch entity
- [ ] MatchingService: recalculate on profile update or scheme sync
- [ ] Match status, reason, missing info, timestamps
- [ ] Incremental recalculation (not full scan every time)

### Frontend
- [ ] My Matches page
- [ ] Match cards with status

---

## Phase 8: Document Vault
**Goal**: Document upload and management

### Backend
- [ ] DocumentStorageService interface (local impl for MVP)
- [ ] UserDocument entity with metadata
- [ ] DocumentController: upload, list, get, delete
- [ ] File validation, hash computation

### Frontend
- [ ] Documents page
- [ ] Upload component with drag-drop
- [ ] Document list with status

---

## Phase 9: Document Extraction
**Goal**: OCR abstraction with mock implementation

### Backend
- [ ] DocumentExtractionService interface
- [ ] MockDocumentExtractionService (returns predefined extracted data)
- [ ] ExtractedDocumentData DTO
- [ ] DocumentExtractedFields entity

### Frontend
- [ ] Extract button on document
- [ ] Extracted fields display

---

## Phase 10: Document Mismatch Engine
**Goal**: Compare profile ↔ document data

### Backend
- [ ] Normalization utilities (names, dates, gender, addresses)
- [ ] DocumentVerificationService
- [ ] Comparison engine with MATCH/PROBABLE_MATCH/MISMATCH/UNKNOWN
- [ ] Severity: CRITICAL/WARNING/INFO
- [ ] DocumentMismatch entity
- [ ] Readiness calculation per scheme

### Tests
- [ ] Exact DOB → MATCH
- [ ] Different DOB → MISMATCH
- [ ] Name "Rahul Kumar" vs "Rahul K." → PROBABLE_MATCH
- [ ] Address minor difference → PROBABLE_MATCH

---

## Phase 11: Notifications
**Goal**: In-app + WhatsApp (mock) notifications

### Backend
- [ ] NotificationService with channel abstraction
- [ ] InAppNotificationProvider
- [ ] MockWhatsAppProvider (logs only)
- [ ] Notification entity
- [ ] Trigger on: new match, critical mismatch, document issues
- [ ] Aggregation (batch multiple schemes)

### Frontend
- [ ] Notifications page
- [ ] Bell icon with count
- [ ] Mark as read

---

## Phase 12: Smart Form Assistant
**Goal**: Dynamic form preparation

### Backend
- [ ] FormTemplate, FormField entities
- [ ] Field types: TEXT, DATE, NUMBER, SELECT, RADIO, CHECKBOX, TEXTAREA, FILE
- [ ] Profile field mapping (e.g., personal.fullName → applicantName)
- [ ] ApplicationDraft entity
- [ ] Draft CRUD APIs

### Frontend
- [ ] Dynamic form renderer
- [ ] Auto-fill from profile
- [ ] Missing field highlighting
- [ ] Save draft, review, download, open official portal

---

## Phase 13: Spring AI Integration
**Goal**: Explanations, translations, mapping assistance

### Backend
- [ ] EligibilityExplanationService (uses structured rule results)
- [ ] DocumentMismatchExplanationService
- [ ] SchemeQAService (chat assistant with tools)
- [ ] FormMappingAssistanceService (AI suggests mappings, admin approves)
- [ ] Provider abstraction (OpenAI, Ollama, etc.)

### Frontend
- [ ] Chat assistant component
- [ ] AI-generated explanations in scheme detail

---

## Phase 14: Admin Dashboard
**Goal**: System monitoring and management

### Backend
- [ ] AdminController: sync status, errors, metrics
- [ ] Mapping approval endpoints

### Frontend
- [ ] Admin layout
- [ ] Sync dashboard
- [ ] Mapping review
- [ ] System metrics

---

## Phase 15: Polish & Demo Readiness
**Goal**: Production-quality MVP

- [ ] Loading/error/empty states
- [ ] Responsive UI
- [ ] Accessibility basics
- [ ] Security review (no secrets, proper auth)
- [ ] Comprehensive tests
- [ ] README with architecture diagram
- [ ] Demo data seeding
- [ ] Demo journey validation

---

## Current Status

| Phase | Status |
|-------|--------|
| 0 - Analysis | ✅ Complete |
| 1 - Foundation | ⬜ Pending |
| 2 - Auth | ⬜ Pending |
| 3 - Profile | ⬜ Pending |
| 4 - MyScheme Sync | ⬜ Pending |
| 5 - Scheme Browse | ⬜ Pending |
| 6 - Eligibility | ⬜ Pending |
| 7 - Matching | ⬜ Pending |
| 8 - Documents | ⬜ Pending |
| 9 - Extraction | ⬜ Pending |
| 10 - Mismatch | ⬜ Pending |
| 11 - Notifications | ⬜ Pending |
| 12 - Smart Form | ⬜ Pending |
| 13 - Spring AI | ⬜ Pending |
| 14 - Admin | ⬜ Pending |
| 15 - Polish | ⬜ Pending |

---

## Next Recommended Task

**Start Phase 1: Project Foundation**
1. Initialize git repository
2. Create Spring Boot backend with Maven
3. Create React frontend with Vite
4. Create docker-compose.yml
5. Create .env.example
6. Create basic README.md
7. Verify all services start