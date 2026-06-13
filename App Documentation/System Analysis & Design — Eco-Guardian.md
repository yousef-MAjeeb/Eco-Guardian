# System Analysis & Design — Eco-Guardian

---

## 1. Actors

| Actor | Description |
|---|---|
| **Citizen (User)** | Primary app user — reports issues, views the community map, tracks report status |
| **AI Service (Gemini 2.5 Flash)** | External API that transforms raw citizen descriptions into formally structured, authority-ready report drafts |

> **Local Authority:** No direct system integration in MVP. The AI generates a professionally structured report the citizen can forward to the relevant authority via their preferred channel (email, portal, etc.).

> **Admin:** There is no admin screen in the app. Platform administration is handled directly via the **Firebase Console**.

---

## 2. Use Case Diagram

```mermaid
graph TB
    subgraph EcoGuardian["Eco-Guardian System"]
        UC1["UC1: Register / Login"]
        UC2["UC2: Submit Environmental Report"]
        UC3["UC3: AI Report Generation"]
        UC4["UC4: View Community Map"]
        UC5["UC5: Track Report Status"]
        UC6["UC6: Browse Clean-up Events"]
        UC7["UC7: Community Resolve Report"]
        UC8["UC8: Receive In-App Notification"]
    end

    Citizen(["👤 Citizen"])
    AIService(["🤖 AI Service — Gemini 2.5 Flash"])

    Citizen --> UC1
    Citizen --> UC2
    Citizen --> UC4
    Citizen --> UC5
    Citizen --> UC6
    Citizen --> UC7
    Citizen --> UC8

    UC2 -->|"triggers"| UC3
    AIService -->|"processes"| UC3
    UC5 -.->|"powered by"| UC8
```

### Use Case Descriptions

| ID | Use Case | Actor | Description |
|---|---|---|---|
| UC1 | Register / Login | Citizen | Create account or sign in via email/password or Google OAuth |
| UC2 | Submit Environmental Report | Citizen | Capture photo, enter description, confirm GPS location, generate AI draft, review, and submit |
| UC3 | AI Report Generation | AI Service | Gemini 2.5 Flash transforms raw citizen input into a structured, authority-ready report draft |
| UC4 | View Community Map | Citizen | Browse geo-pinned submitted reports on a live Google Map |
| UC5 | Track Report Status | Citizen | Monitor the lifecycle of submitted reports from Submitted to Resolved |
| UC6 | Browse Clean-up Events | Citizen | View upcoming community clean-up events by location and date — **read-only** |
| UC7 | Community Resolve Report | Citizen | Nearby citizen marks a report as resolved with an optional follow-up photo — requires GPS proximity check |
| UC8 | Receive In-App Notification | Citizen | Notified via Firestore real-time listener when their report status changes |

---

## 3. Software Architecture Diagram

```mermaid
graph TB
    subgraph AndroidApp["Android App — Kotlin + Jetpack Compose"]
        subgraph Presentation["Presentation Layer"]
            UI["Jetpack Compose UI\nScreens and Components"]
            VM["ViewModels\nStateFlow"]
        end
        subgraph Domain["Domain Layer"]
            UC_L["Use Cases\nBusiness Logic"]
            DM["Domain Models\nPure Data Classes"]
        end
        subgraph Data["Data Layer"]
            Repo["Repositories\nSingle Source of Truth"]
            LocalDS["Local Data Source\nRoom DB"]
            RemoteDS["Remote Data Source\nRetrofit + Firebase SDK"]
        end
        Koin["Koin\nDependency Injection"]
    end

    subgraph FirebaseBackend["Firebase Backend — Spark Free Plan"]
        FB_Auth["Firebase Auth"]
        FB_Firestore["Cloud Firestore"]
        FB_Storage["Firebase Storage"]
    end

    subgraph ExternalAPIs["External APIs"]
        AI["Google Gemini API\nvia Retrofit — Free Tier"]
        GMaps["Google Maps SDK\nCompose Integration"]
        GOAuth["Google OAuth 2.0"]
    end

    UI --> VM
    VM --> UC_L
    UC_L --> DM
    UC_L --> Repo
    Repo --> LocalDS
    Repo --> RemoteDS
    RemoteDS --> FB_Auth
    RemoteDS --> FB_Firestore
    RemoteDS --> FB_Storage
    RemoteDS --> AI
    FB_Auth --> GOAuth
    UI --> GMaps

    Koin -.->|"injects"| VM
    Koin -.->|"injects"| UC_L
    Koin -.->|"injects"| Repo
```

> **Notes:**
> - `UI --> GMaps` — the Google Maps composable renders in the UI layer but **all map data (pins, camera position) is supplied by the ViewModel via StateFlow**. The UI fetches nothing directly.
> - **Gemini API** is called via Retrofit from the Data layer. The key is obtained free from Google AI Studio — no credit card required. Key stored in `local.properties`, never committed to Git, accessed via `BuildConfig`.
> - **Room** caches offline report drafts so user input is never lost if the network drops mid-submission.
> - **No Cloud Functions** — the entire backend is driven by the Firebase SDK and direct Retrofit calls.

---

## 4. Database Design & Data Modelling

### ER Diagram

```mermaid
erDiagram
    USER {
        string userID PK
        string name
        string email
        string role
        string profilePhotoURL
        float homeLatitude
        float homeLongitude
        datetime createdAt
    }

    REPORT {
        string reportID PK
        string userID FK
        string title
        string rawDescription
        string aiGeneratedReport
        string photoURL
        string category
        string status
        float latitude
        float longitude
        datetime createdAt
        datetime updatedAt
    }

    EVENT {
        string eventID PK
        string organizerID FK
        string title
        string description
        float latitude
        float longitude
        datetime eventDate
        int capacity
    }

    NOTIFICATION {
        string notificationID PK
        string userID FK
        string reportID FK
        string message
        boolean isRead
        datetime createdAt
    }

    COMMENT {
        string commentID PK
        string reportID FK
        string userID FK
        string content
        datetime createdAt
    }

    USER ||--o{ REPORT : "submits"
    USER ||--o{ COMMENT : "writes"
    USER ||--o{ NOTIFICATION : "receives"
    REPORT ||--o{ COMMENT : "has"
    REPORT ||--o{ NOTIFICATION : "triggers"
```

### Logical Schema

**USER** (`userID` PK, `name`, `email`, `role` [citizen | admin], `profilePhotoURL`, `homeLatitude`, `homeLongitude`, `createdAt`)

> `passwordHash` is **intentionally absent** — Firebase Authentication manages all credentials securely. Passwords must never be stored in Firestore.
> `homeLatitude` / `homeLongitude` store the user's **saved home location preference** (FR-22), not their real-time GPS position.

---

**REPORT** (`reportID` PK, `userID` FK → USER, `title`, `rawDescription`, `aiGeneratedReport`, `photoURL`, `category` [litter | pollution | dumping | other], `status` [submitted | resolved | cancelled], `latitude`, `longitude`, `createdAt`, `updatedAt`)

> The AI generates a formally structured, authority-ready report draft. The citizen may forward this to the relevant local authority via their preferred channel. No direct authority system integration exists in MVP.

---

**EVENT** (`eventID` PK, `organizerID` FK → USER, `title`, `description`, `latitude`, `longitude`, `eventDate`, `capacity`)

> Events are **read-only listings** in MVP. Citizen registration for events is a future enhancement.

---

**NOTIFICATION** (`notificationID` PK, `userID` FK → USER, `reportID` FK → REPORT, `message`, `isRead`, `createdAt`)

---

**COMMENT** (`commentID` PK, `reportID` FK → REPORT, `userID` FK → USER, `content`, `createdAt`)

---

## 5. Data Flow & System Behaviour

### 5.1 Context-Level DFD (Level 0)

```mermaid
flowchart LR
    Citizen(["👤 Citizen"])
    AI_Ext(["🤖 Gemini 2.5 Flash API"])

    Sys[["Eco-Guardian\nAndroid App"]]

    Citizen -->|"Photo + Description + Location"| Sys
    Sys -->|"Report Status and Notifications"| Citizen
    Sys -->|"Raw description + category"| AI_Ext
    AI_Ext -->|"Structured report draft"| Sys
```

---

### 5.2 Level 1 DFD

```mermaid
flowchart TD
    Citizen(["👤 Citizen"])
    AI(["🤖 Gemini 2.5 Flash API"])

    P1["1.0\nUser Authentication"]
    P2["2.0\nReport Submission"]
    P3["3.0\nAI Report Generation\nvia Gemini API"]
    P4["4.0\nMap and Community View"]
    P5["5.0\nEvent Browsing"]
    P6["6.0\nNotification Service\nFirestore listener"]
    P7["7.0\nCommunity Resolution\nGPS proximity check"]

    DS1[("User Store\nFirestore")]
    DS2[("Report Store\nFirestore + Storage")]
    DS3[("Event Store\nFirestore")]
    DS4[("Notification Store\nFirestore")]

    Citizen -->|"credentials"| P1
    P1 <--> DS1
    P1 -->|"auth token"| Citizen

    Citizen -->|"photo + description + location"| P2
    P2 -->|"raw input"| P3
    P3 <-->|"Retrofit HTTP call"| AI
    P3 -->|"structured report draft"| P2
    P2 <--> DS2
    P2 -->|"submission confirmed"| Citizen

    DS2 -->|"report pins"| P4
    P4 -->|"map view"| Citizen

    Citizen -->|"event query"| P5
    P5 <--> DS3
    P5 -->|"event listings"| Citizen

    DS2 -->|"status change event"| P6
    P6 <--> DS4
    P6 -->|"in-app notification"| Citizen

    Citizen -->|"resolve + optional photo + GPS"| P7
    P7 <--> DS2
    P7 -->|"status updated to Resolved"| Citizen
```

---

### 5.3 Sequence Diagram — Report Submission Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Android App
    participant VM as ViewModel
    participant UC as SubmitReportUseCase
    participant Repo as Repository
    participant Storage as Firebase Storage
    participant Firestore as Cloud Firestore
    participant AI as Gemini API (Retrofit)

    User->>App: Opens Report Issue screen
    User->>App: Captures photo and enters description
    App->>VM: onGenerateClicked(photo, description, location, category)
    VM->>UC: execute(photo, description, location, category)

    UC->>Repo: uploadPhoto(photo)
    Repo->>Storage: PUT image binary
    Storage-->>Repo: photoURL

    UC->>Repo: generateAIReport(description, category)
    Repo->>AI: POST /v1/generateContent via Retrofit
    AI-->>Repo: structured report JSON
    Repo-->>UC: aiReport text

    UC-->>VM: Result.Success(aiReport)
    VM-->>App: update state — navigate to Preview screen

    User->>App: Reviews AI draft — edits inline if needed
    User->>App: Taps Confirm and Submit

    App->>VM: onConfirmClicked(finalReport)
    VM->>UC: saveReport(finalReport)
    UC->>Repo: saveReport(reportData)
    Repo->>Firestore: write to /reports collection
    Firestore-->>Repo: success
    Repo-->>UC: Result.Success
    UC-->>VM: Result.Success
    VM-->>App: update state — navigate to Success screen
    App-->>User: Your report has been submitted!
```

---

### 5.4 Sequence Diagram — Community Resolution Flow

```mermaid
sequenceDiagram
    actor NearbyUser as Nearby Citizen
    participant App as Android App
    participant VM as ViewModel
    participant UC as ResolveReportUseCase
    participant Repo as Repository
    participant Firestore as Cloud Firestore

    NearbyUser->>App: Taps report pin on map
    App->>VM: onReportSelected(reportID)
    VM->>UC: getReport(reportID)
    UC->>Repo: fetchReport(reportID)
    Repo->>Firestore: GET /reports/{reportID}
    Firestore-->>Repo: report data
    Repo-->>UC: Report
    UC-->>VM: Report
    VM-->>App: show Report Detail screen with Resolve button

    NearbyUser->>App: Taps Mark as Resolved
    App->>VM: onResolveClicked(reportID, userLocation)
    VM->>UC: execute(reportID, userLocation)

    UC->>UC: check GPS distance to report location
    alt User is NOT within proximity radius
        UC-->>VM: Result.Error(TooFarAway)
        VM-->>App: show error — you must be near the issue to resolve it
    else User is within proximity radius
        UC->>Repo: updateStatus(reportID, RESOLVED)
        Repo->>Firestore: update /reports/{reportID} status
        Firestore-->>Repo: success
        Repo-->>UC: Result.Success
        UC-->>VM: Result.Success
        VM-->>App: update state — report marked as Resolved
        App-->>NearbyUser: Thank you for confirming this issue is resolved!
    end
```

---

### 5.5 Sequence Diagram — User Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Android App
    participant FireAuth as Firebase Auth
    participant Firestore as Firestore DB

    User->>App: Enters email and password OR taps Google Sign-In
    App->>FireAuth: signInWithEmailAndPassword() or signInWithGoogle()
    FireAuth-->>App: Firebase ID Token

    App->>Firestore: GET /users/{userID}

    alt User profile exists
        Firestore-->>App: User profile data
        App-->>User: Navigate to Home Screen
    else First-time login
        Firestore-->>App: Document not found
        App->>Firestore: POST /users/{userID} — create profile
        Firestore-->>App: Profile created
        App-->>User: Navigate to Onboarding Screen
    end
```

---

### 5.6 Activity Diagram — End-to-End Report Submission

```mermaid
flowchart TD
    Start(["User Opens App"]) --> CheckLogin{"Already\nLogged In?"}
    CheckLogin -->|No| Auth["Login / Register Screen"]
    Auth --> Home
    CheckLogin -->|Yes| Home["Home Screen"]

    Home --> TapReport["Tap Report Issue"]
    TapReport --> CheckCamPerm{"Camera\nPermission?"}
    CheckCamPerm -->|Granted| Camera
    CheckCamPerm -->|Denied| RequestCam["Request Camera Permission"]
    RequestCam --> PermResult{"Result"}
    PermResult -->|Granted| Camera["Open Camera / Gallery"]
    PermResult -->|Permanently Denied| ShowRationale["Show Rationale Screen\nGuide user to Settings"]
    ShowRationale --> Home

    Camera --> Photo["Capture or Select Photo"]
    Photo --> Form["Fill Description\nSelect Category\nConfirm GPS Location"]
    Form --> Generate["Tap Generate AI Report"]
    Generate --> CallAI["Call Gemini API via Retrofit"]

    CallAI --> AIResult{"AI Call\nSuccessful?"}
    AIResult -->|Failure or Timeout| Fallback["Show error — offer to\nsubmit raw description instead"]
    AIResult -->|Success| Preview["Display AI Draft Preview\neditable text field"]

    Preview --> Satisfied{"Happy with\nthe draft?"}
    Satisfied -->|"No — edit inline"| EditDraft["Edit draft text\ndirectly on Preview screen"]
    EditDraft --> Satisfied
    Satisfied -->|Yes| Submit["Tap Confirm and Submit"]
    Fallback --> Submit

    Submit --> Upload["Upload Photo to\nFirebase Storage"]
    Upload --> Save["Save Report to\nCloud Firestore"]
    Save --> Pin["Pin appears on\nCommunity Map"]
    Pin --> Success(["Success Screen\nReport Submitted!"])
```

---

### 5.7 State Diagram — Report Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Submitted : Citizen submits report

    Submitted --> Resolved : Nearby community member\nconfirms issue is fixed\n(GPS proximity check)
    Submitted --> Cancelled : Reporter cancels their own report

    Resolved --> Submitted : Any user reopens —\nissue has reappeared

    Cancelled --> [*]
    Resolved --> [*]
```

---

### 5.8 Class Diagram

> **Design rule:** Domain models are **pure data classes** — they hold state only. All operations live in **Use Cases** and **Repositories**.

```mermaid
classDiagram

    class User {
        +String userID
        +String name
        +String email
        +String role
        +String profilePhotoURL
        +Double homeLatitude
        +Double homeLongitude
        +DateTime createdAt
    }

    class Report {
        +String reportID
        +String userID
        +String title
        +String rawDescription
        +String aiGeneratedReport
        +String photoURL
        +String category
        +ReportStatus status
        +Double latitude
        +Double longitude
        +DateTime createdAt
        +DateTime updatedAt
    }

    class ReportStatus {
        <<enumeration>>
        SUBMITTED
        RESOLVED
        CANCELLED
    }

    class Event {
        +String eventID
        +String organizerID
        +String title
        +String description
        +Double latitude
        +Double longitude
        +DateTime eventDate
        +Int capacity
    }

    class Notification {
        +String notificationID
        +String userID
        +String reportID
        +String message
        +Boolean isRead
        +DateTime createdAt
    }

    class Comment {
        +String commentID
        +String reportID
        +String userID
        +String content
        +DateTime createdAt
    }

    class SubmitReportUseCase {
        +execute(photo, description, location, category) Result
    }

    class GenerateAIReportUseCase {
        +execute(description String, category String) String
    }

    class ResolveReportUseCase {
        +execute(reportID String, userLocation LatLng) Result
    }

    class GetReportsUseCase {
        +execute(filter ReportFilter) List~Report~
    }

    class AuthUseCase {
        +signIn(email String, password String) Result
        +signInWithGoogle() Result
        +signOut()
    }

    class ReportRepository {
        +uploadPhoto(photo ByteArray) String
        +generateAIReport(description String, category String) String
        +saveReport(report Report) Result
        +getReports(filter ReportFilter) List~Report~
        +getReportsByLocation(lat Double, lng Double, radius Double) List~Report~
        +updateStatus(reportID String, status ReportStatus) Result
    }

    class EventRepository {
        +getEvents(lat Double, lng Double, radius Double) List~Event~
    }

    class AuthRepository {
        +signIn(email String, password String) Result
        +signInWithGoogle() Result
        +signOut()
        +getCurrentUser() User
    }

    User "1" --> "0..*" Report : submits
    User "1" --> "0..*" Comment : writes
    User "1" --> "0..*" Notification : receives
    Report "1" --> "1" ReportStatus : has status
    Report "1" --> "0..*" Comment : contains
    Report "1" --> "0..*" Notification : triggers

    SubmitReportUseCase --> ReportRepository : uses
    GenerateAIReportUseCase --> ReportRepository : uses
    ResolveReportUseCase --> ReportRepository : uses
    GetReportsUseCase --> ReportRepository : uses
    AuthUseCase --> AuthRepository : uses

    ReportRepository ..> Report : manages
    EventRepository ..> Event : manages
    AuthRepository ..> User : manages
```
