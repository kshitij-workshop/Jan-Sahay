# Implementation Plan - Government Scheme Assistant

## Phase 0: Repository Analysis (COMPLETED)
- [x] Inspected repository - empty directory
- [x] No existing codebase
- [x] No git repository initialized
- [x] Technology stack confirmed: Spring Boot (Java), React (Vite), MySQL

---

## Phase 1: Project Foundation (COMPLETED)
**Goal**: Spring Boot + React + MySQL + Docker Compose running locally

### Backend Setup
- [x] Spring Boot 3.x project with Maven
- [x] Dependencies: Spring Web, Spring Data JPA, Spring Security, Spring AI, MySQL Driver, Flyway, JWT (jjwt), Validation
- [x] Application.yml with profiles (dev, prod, test)
- [x] Health endpoint (/api/health + /actuator/health)
- [x] Global exception handler
- [x] Docker Compose with MySQL

### Frontend Setup  
- [x] React + Vite + TypeScript
- [x] React Router, Axios
- [x] Basic layout with header, sidebar
- [x] Proxy configuration for API calls

### Infrastructure
- [x] docker-compose.yml (MySQL, backend, frontend)
- [x] .env.example with all placeholders
- [x] README.md with setup instructions
- [x] Git initialization

**Verification**: `docker-compose up` starts all services, health endpoints respond

---

## Phase 2: Authentication (COMPLETED)
**Goal**: Secure JWT-based auth with USER/ADMIN roles

### Backend
- [x] User entity, UserRepository
- [x] AuthController: register, login, refresh, verify-email, resend-verification, me
- [x] JWT token generation/validation with access/refresh type claims
- [x] Spring Security configuration (explicit `/api` route prefixes, no context-path coupling)
- [x] BCrypt password encoding
- [x] Role-based authorization (USER, ADMIN) via URL rules + `@PreAuthorize` (AdminController ping)
- [x] Email verification flow (token + expiry, non-blocking send, configurable frontend URL)

### Frontend
- [x] Login/Register pages
- [x] Auth context + token management (incl. `isAdmin`)
- [x] Protected routes + AdminRoute guard
- [x] Axios interceptor for JWT (incl. refresh queue)
- [x] Verify-email page + Admin access-check page

### Tests
- [x] AuthController integration tests (11 tests: register/login/refresh/me/verify/resend/RBAC/validation)
- [x] Security config tests (via admin ping: anonymous→403, USER→403, ADMIN→200)

**Decisions**:
- Removed `server.servlet.context-path=/api`; controllers carry explicit `/api` prefixes so
  SecurityConfig matchers behave identically in production and MockMvc tests.
- Refresh tokens carry `type=refresh` and are rejected by the auth filter and `/me`;
  access tokens carry `type=access` (+ role claim) and are rejected by `/refresh`.
- `EmailService` never fails registration (logs warning) so demo mode works without SMTP.
- Lombok removed from main code paths (annotation processor not configured); explicit
  constructors/getters/setters are used instead.

---

## Phase 3: User Profile (COMPLETED)
**Goal**: Complete citizen profile management

### Backend
- [x] Profile entities: UserProfile (1-1 with users, shared PK), UserAddress (1-N)
- [x] ProfileController: GET/PUT /api/profile (principal-scoped, cross-user access impossible)
- [x] Validation (Bean Validation: past DOB, pincode pattern, income/dependents/disability ranges)
- [x] Profile completeness calculation (section-weighted %, dot-path missingFields)

### Frontend
- [x] Profile page with sections (API-driven, server completeness per section)
- [x] Form validation (client hints + server error details)
- [x] Progress indicator (overall bar + per-section %, missing-fields banner)

**Decisions**:
- Enums stored as VARCHAR (never MySQL ENUM) so new categories need no DDL.
- Wrapper types throughout: `null` = unknown, feeding future INSUFFICIENT_INFORMATION.
- `additional_attributes JSON` escape hatch for future scheme criteria without schema rewrites.
- PUT merges scalars on non-null; a present `addresses` list replaces the stored set
  (exactly one primary enforced server-side).
- Malformed values → 400; absent values → completeness `missingFields`, never errors.
- `PROFILE_UPDATED` structured log carries only the opaque user id, no PII.

---

## Phase 4: MyScheme Integration & Sync (COMPLETED)
**Goal**: Fetch, normalize, store government schemes

### Backend
- [x] MySchemeClient (RestClient) with API key from env (`x-api-key`, never leaves backend)
- [x] Scheme entities: Scheme, SchemeRawData, SchemeTag, SchemeState, SchemeFaq, SchemeDocument, SchemeApplicationStep
- [x] Raw JSON storage (scheme_raw_data table, verbatim payloads)
- [x] SyncService: paginated fetch → detail fetch → FAQs → documents → normalize → store
- [x] Admin endpoint: POST /api/admin/schemes/sync (+ job/error inspection)
- [x] Sync job tracking (sync_jobs, sync_errors tables)
- [x] Retry logic (429/5xx/transport, exponential backoff), controlled concurrency (bounded pool)
- [x] Development limit: maxSchemes config (default 50, admin-overridable but capped)

### Frontend
- [x] Admin sync trigger button
- [x] Sync status display (job history with outcome counts)

**Decisions**:
- Live API returns 403 without a valid key and the local key is a placeholder,
  so the pipeline builds against the documented response shape with a stub client
  for keyless demo/tests (selected automatically when `MYSCHEME_API_KEY` is blank).
- Normalization is best-effort and never invents: unknown/missing fields stay null;
  raw JSON is always preserved. Search/detail parsers tolerate envelope variants.
- Upserts are idempotent by slug (reruns count as updates); resume = rerun from offset.
- Tests pin the stub client so shell env can never flip tests onto the live API.
- Offline dump import (`POST /api/admin/schemes/import`, `data/schemes.json`):
  V5 columns/tables, idempotent by slug, verified live against MySQL —
  823/823 imported with 0 failures (9892 FAQs, 11179 steps, 4088 tags),
  rerun yields 823 updates and 0 duplicates.

---

## Phase 5: Scheme Browsing (COMPLETED)
**Goal**: Citizen-facing scheme discovery

### Backend
- [x] SchemeController: search, filters, pagination, detail, FAQs, documents
- [x] Search by: text, category, state, level (+ distinct categories endpoint)
- [x] Scheme detail DTO with all sections
- [ ] Demographic filters (gender, age, student, occupation, caste, disability) — moved to Phase 6/7 eligibility engine

### Frontend
- [x] Schemes listing page with filters (debounced search, pagination)
- [x] Scheme detail page (overview, benefits, eligibility-as-published, documents, process, FAQs, official link)
- [x] Scheme card component (no invented eligibility badges)

**Decisions**:
- No eligibility verdicts anywhere in browsing UI until the deterministic Phase 6 engine exists.
- Category/state filters consult child tables; All-India schemes match every state filter.
- Detail page labels publisher text as such and links official sources; platform never auto-submits.
- Verified live against 823 imported schemes: 62 scholarship hits, 123 categories.

---

## Phase 6: Eligibility Engine (COMPLETED)
**Goal**: Deterministic rule-based eligibility with explanations

### Backend
- [x] Rule interface: `EligibilityRule` with `evaluate(profile, scheme)`
- [x] Rules: AgeRule, StateRule, GenderRule, IncomeRule, CasteRule, OccupationRule, StudentRule, DisabilityRule, ResidenceRule, EmploymentRule, BplRule, MaritalStatusRule, MinorityRule
- [x] EligibilityService orchestrating rules (+ `GET /api/schemes/{id}/eligibility`, auth-required)
- [x] Result: ELIGIBLE / NOT_ELIGIBLE / INSUFFICIENT_INFORMATION
- [x] Missing fields identification (dot-paths)
- [x] Structured rule results with pass/fail/missing/skip + messages

### Tests (Critical)
- [x] Age 22, req 18-70 → PASS
- [x] Age 17, req 18-70 → FAIL
- [x] Missing age → INSUFFICIENT_INFORMATION
- [x] Bihar user + Bihar scheme → PASS
- [x] Other state + Bihar-only → FAIL
- [x] Missing income → INSUFFICIENT_INFORMATION

**Decisions**:
- Rules evaluate admin-curated `scheme_eligibility` rows only; unconstrained dimensions
  SKIP, missing user data yields MISSING (never FAIL). Free text is display-only.
- No criteria row = no machine-readable constraints (outcome reflects completeness only).
- Refresh/access token types enforced; eligibility endpoint requires authentication.

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
| 1 - Foundation | ✅ Complete |
| 2 - Auth | ✅ Complete |
| 3 - Profile | ✅ Complete |
| 4 - MyScheme Sync | ✅ Complete |
| 5 - Scheme Browse | ✅ Complete |
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