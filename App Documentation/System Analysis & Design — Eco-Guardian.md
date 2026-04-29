
## 2. Use Case Diagram & Descriptions

### Actors

|Actor|Description|
|---|---|
|**Citizen (User)**|Primary app user; reports issues, views map, attends events|
|**Admin**|Platform manager; oversees reports, users, and events|
|**Local Authority**|Receives reports and updates their resolution status|
|**AI Service (Claude)**|External API that transforms raw descriptions into structured reports|

### Use Case Diagram

```mermaid
graph TB
    subgraph Eco-Guardian System
        UC1[Register / Login]
        UC2[Submit Environmental Report]
        UC3[AI Report Generation]
        UC4[View Community Map]
        UC5[Track Report Status]
        UC6[Browse Clean-up Events]
        UC7[Register for Event]
        UC8[Receive Push Notifications]
        UC9[Manage Reports]
        UC10[Manage Events]
        UC11[Manage Users]
        UC12[Receive Reports]
        UC13[Update Report Status]
    end

    Citizen([👤 Citizen])
    Admin([🛡️ Admin])
    Authority([🏛️ Local Authority])
    AIService([🤖 AI Service])

    Citizen -->|uses| UC1
    Citizen -->|uses| UC2
    Citizen -->|uses| UC4
    Citizen -->|uses| UC5
    Citizen -->|uses| UC6
    Citizen -->|uses| UC7
    Citizen -->|uses| UC8

    UC2 -->|triggers| UC3
    AIService -->|processes| UC3

    Admin -->|manages| UC9
    Admin -->|manages| UC10
    Admin -->|manages| UC11

    Authority -->|uses| UC12
    Authority -->|uses| UC13
    UC13 -->|triggers| UC8
```

### Use Case Descriptions

|ID|Use Case|Actor|Description|
|---|---|---|---|
|UC1|Register / Login|Citizen|User creates account or logs in via email or Google OAuth|
|UC2|Submit Environmental Report|Citizen|Capture photo, enter description, select category & GPS location|
|UC3|AI Report Generation|AI Service|Transforms raw citizen input into a structured, authority-ready report|
|UC4|View Community Map|Citizen|Browse geo-pinned reports around the user's neighbourhood|
|UC5|Track Report Status|Citizen|Monitor report lifecycle from SUBMITTED → RESOLVED|
|UC6|Browse Clean-up Events|Citizen|View upcoming community events by location and date|
|UC7|Register for Event|Citizen|Sign up to attend a clean-up event|
|UC8|Receive Notifications|Citizen|Get push notifications when report status changes|
|UC9|Manage Reports|Admin|Review, approve, escalate, or close reports|
|UC10|Manage Events|Admin|Create, edit, or cancel community events|
|UC11|Manage Users|Admin|View, suspend, or remove user accounts|
|UC12|Receive Reports|Local Authority|View incoming structured reports within their jurisdiction|
|UC13|Update Report Status|Local Authority|Change report status as investigation proceeds|

---

###  Software Architecture Diagram

```mermaid
graph TB
    subgraph AndroidApp [Android App - Kotlin]
        subgraph Presentation [Presentation Layer]
            UI[Jetpack Compose UI\nScreens & Components]
            VM[ViewModels\nStateFlow / LiveData]
        end
        subgraph Domain [Domain Layer]
            UC_L[Use Cases\nBusiness Logic]
            DM[Domain Models]
        end
        subgraph Data [Data Layer]
            Repo[Repositories\nSingle Source of Truth]
            LocalDS[Local Data Source\nRoom DB]
            RemoteDS[Remote Data Source\nRetrofit + Firebase SDK]
        end
    end

    subgraph Firebase [Firebase Backend]
        FB_Auth[Firebase Auth]
        FB_Firestore[Cloud Firestore]
        FB_Storage[Firebase Storage]
        FB_FCM[Firebase FCM]
        CF[Cloud Functions\nNode.js]
    end

    subgraph ExternalAPIs [External APIs]
        AI[Anthropic Claude API]
        GMaps[Google Maps API]
        GOAuth[Google OAuth 2.0]
    end

    UI --> VM --> UC_L --> Repo
    Repo --> LocalDS
    Repo --> RemoteDS
    RemoteDS --> FB_Auth
    RemoteDS --> FB_Firestore
    RemoteDS --> FB_Storage
    RemoteDS --> CF
    CF --> AI
    FB_Firestore --> FB_FCM
    FB_Auth --> GOAuth
    UI --> GMaps
```

---

## 5. Database Design & Data Modeling

### ER Diagram

```mermaid
erDiagram
    USER {
        string userID PK
        string name
        string email
        string passwordHash
        string role
        string profilePhotoURL
        float latitude
        float longitude
        datetime createdAt
    }

    REPORT {
        string reportID PK
        string userID FK
        string authorityID FK
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

    AUTHORITY {
        string authorityID PK
        string name
        string jurisdiction
        string contactEmail
        string phone
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
        int registeredCount
    }

    EVENT_REGISTRATION {
        string registrationID PK
        string eventID FK
        string userID FK
        datetime registeredAt
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
    USER ||--o{ EVENT_REGISTRATION : "registers for"
    USER ||--o{ COMMENT : "writes"
    USER ||--o{ NOTIFICATION : "receives"
    USER ||--o{ EVENT : "organizes"
    EVENT ||--o{ EVENT_REGISTRATION : "has"
    REPORT ||--o{ COMMENT : "has"
    REPORT ||--o{ NOTIFICATION : "triggers"
    AUTHORITY ||--o{ REPORT : "receives"
```

### Logical Schema (Key Tables)

**USER** (`userID` PK, `name`, `email`, `passwordHash`, `role` [citizen|admin|authority], `profilePhotoURL`, `latitude`, `longitude`, `createdAt`)

**REPORT** (`reportID` PK, `userID` FK → USER, `authorityID` FK → AUTHORITY, `title`, `rawDescription`, `aiGeneratedReport`, `photoURL`, `category` [dumping|pollution|litter|other], `status` [draft|submitted|under_review|in_progress|escalated|resolved|rejected], `latitude`, `longitude`, `createdAt`, `updatedAt`)

**AUTHORITY** (`authorityID` PK, `name`, `jurisdiction`, `contactEmail`, `phone`)

**EVENT** (`eventID` PK, `organizerID` FK → USER, `title`, `description`, `latitude`, `longitude`, `eventDate`, `capacity`, `registeredCount`)

**EVENT_REGISTRATION** (`registrationID` PK, `eventID` FK → EVENT, `userID` FK → USER, `registeredAt`)

**NOTIFICATION** (`notificationID` PK, `userID` FK → USER, `reportID` FK → REPORT, `message`, `isRead`, `createdAt`)

**COMMENT** (`commentID` PK, `reportID` FK → REPORT, `userID` FK → USER, `content`, `createdAt`)

---

## 6. Data Flow & System Behavior

### 6.1 Context-Level DFD (Level 0)

```mermaid
flowchart LR
    Citizen([👤 Citizen])
    Authority([🏛️ Local Authority])
    Admin([🛡️ Admin])
    AI_Ext([🤖 AI Service])

    Sys[[Eco-Guardian\nSystem]]

    Citizen -->|"Photo + Description + Location"| Sys
    Sys -->|"Report Status & Notifications"| Citizen
    Sys -->|"Structured Reports"| Authority
    Authority -->|"Status Updates"| Sys
    Admin -->|"Management Commands"| Sys
    Sys -->|"Analytics & Reports"| Admin
    Sys -->|"Raw Report Content"| AI_Ext
    AI_Ext -->|"Structured Report Text"| Sys
```

### 6.2 Level 1 DFD

```mermaid
flowchart TD
    Citizen([👤 Citizen])
    Authority([🏛️ Authority])
    AI([🤖 AI Service])

    P1[1.0\nUser Authentication]
    P2[2.0\nReport Submission]
    P3[3.0\nAI Report Generation]
    P4[4.0\nMap & Community View]
    P5[5.0\nEvent Management]
    P6[6.0\nNotification Service]

    DS1[(User Store)]
    DS2[(Report Store)]
    DS3[(Event Store)]
    DS4[(Notification Store)]

    Citizen -->|credentials| P1
    P1 <--> DS1
    P1 -->|auth token| Citizen

    Citizen -->|"photo + description\n+ location"| P2
    P2 -->|raw input| P3
    P3 <-->|API call| AI
    P3 -->|structured report| DS2
    P2 <--> DS2
    P2 -->|submission confirmed| Citizen

    DS2 -->|report pins| P4
    P4 -->|map view| Citizen

    Citizen -->|event query/register| P5
    P5 <--> DS3
    P5 -->|event data| Citizen

    DS2 -->|status change event| P6
    P6 <--> DS4
    P6 -->|push notification| Citizen

    DS2 -->|new report| Authority
    Authority -->|status update| DS2
```

### 6.3 Sequence Diagram — Report Submission Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Android App
    participant VM as ViewModel
    participant Repo as Repository
    participant Storage as Firebase Storage
    participant Firestore as Cloud Firestore
    participant CF as Cloud Function
    participant AI as Claude AI API

    User->>App: Opens "Report Issue" screen
    User->>App: Captures photo & enters description
    App->>VM: submitReport(photo, description, location, category)
    VM->>Repo: uploadPhoto(photo)
    Repo->>Storage: PUT image binary
    Storage-->>Repo: returns photoURL
    VM->>Repo: generateAIReport(description, category, photoURL)
    Repo->>CF: POST /generateReport {description, category}
    CF->>AI: Claude API prompt with raw description
    AI-->>CF: Structured report JSON
    CF-->>Repo: aiReport text
    Repo-->>VM: AI report ready
    VM-->>App: Display AI Report Preview screen
    User->>App: Reviews & confirms report
    VM->>Repo: saveReport(reportData)
    Repo->>Firestore: write to /reports collection
    Firestore-->>Repo: success
    Repo->>Firestore: notify matched Authority
    VM-->>App: Show success confirmation
    App-->>User: "Your report has been submitted!"
```

### 6.4 Sequence Diagram — User Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant App as Android App
    participant FireAuth as Firebase Auth
    participant Firestore as Firestore DB

    User->>App: Enters email + password OR taps Google Sign-In
    App->>FireAuth: signInWithEmailAndPassword() / signInWithGoogle()
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

### 6.5 Activity Diagram — End-to-End Report Submission

```mermaid
flowchart TD
    Start([🚀 User Opens App]) --> CheckLogin{Already\nLogged In?}
    CheckLogin -->|No| Auth[Login / Register Screen]
    Auth --> Home
    CheckLogin -->|Yes| Home[🏠 Home Screen]

    Home --> TapReport[Tap Report Issue Button]
    TapReport --> Camera[Open Camera / Gallery]
    Camera --> Photo[Capture or Select Photo]
    Photo --> Form[Fill Description\nSelect Category\nConfirm GPS Location]
    Form --> Generate[Tap Generate AI Report]
    Generate --> CallAI[Call Claude AI API\nvia Cloud Function]
    CallAI --> Preview[Display AI Report Preview]

    Preview --> Satisfied{User Happy\nWith Report?}
    Satisfied -->|No - Edit| Form
    Satisfied -->|Yes| Submit[Tap Submit]

    Submit --> Upload[Upload Photo to\nFirebase Storage]
    Upload --> Save[Save Report to\nCloud Firestore]
    Save --> Route[Auto-Route to\nLocal Authority]
    Route --> Pin[Pin on\nCommunity Map]
    Pin --> Notify[Send Push Notification\nto Authority]
    Notify --> Success([✅ Success Screen\nReport Submitted!])
```

### 6.6 State Diagram — Report Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Draft : User starts a report

    Draft --> Submitted : User confirms & submits
    Draft --> Cancelled : User discards draft

    Submitted --> UnderReview : Authority acknowledges receipt
    Submitted --> Rejected : Authority rejects (invalid / duplicate)

    UnderReview --> InProgress : Authority begins field action
    UnderReview --> Rejected : Determined non-actionable

    InProgress --> Resolved : Issue fully addressed & closed
    InProgress --> Escalated : Requires higher authority intervention

    Escalated --> InProgress : Escalation handled, returns to action
    Escalated --> Resolved : Resolved after escalation

    Rejected --> [*]
    Cancelled --> [*]
    Resolved --> [*]
```

### 6.7 Class Diagram

```mermaid
classDiagram
    class User {
        +String userID
        +String name
        +String email
        +String role
        +String profilePhotoURL
        +Double latitude
        +Double longitude
        +DateTime createdAt
        +login()
        +logout()
        +updateProfile()
    }

    class Report {
        +String reportID
        +String userID
        +String authorityID
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
        +submit()
        +updateStatus(status ReportStatus)
        +addComment(comment Comment)
    }

    class ReportStatus {
        <<enumeration>>
        DRAFT
        SUBMITTED
        UNDER_REVIEW
        IN_PROGRESS
        ESCALATED
        RESOLVED
        REJECTED
        CANCELLED
    }

    class AIReportService {
        -String apiEndpoint
        +generateReport(description String, category String) String
        +buildPrompt(input RawReportInput) String
        +parseResponse(response String) StructuredReport
    }

    class Authority {
        +String authorityID
        +String name
        +String jurisdiction
        +String contactEmail
        +receiveReport(report Report)
        +updateReportStatus(reportID String, status ReportStatus)
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
        +Int registeredCount
        +register(userID String)
        +cancel()
        +getAvailableSpots() Int
    }

    class EventRegistration {
        +String registrationID
        +String eventID
        +String userID
        +DateTime registeredAt
    }

    class Notification {
        +String notificationID
        +String userID
        +String reportID
        +String message
        +Boolean isRead
        +DateTime createdAt
        +markAsRead()
    }

    class Comment {
        +String commentID
        +String reportID
        +String userID
        +String content
        +DateTime createdAt
        +delete()
    }

    class ReportRepository {
        +submitReport(report Report) Result
        +getReports(filter ReportFilter) List~Report~
        +getReportsByLocation(lat Double, lng Double, radius Double) List~Report~
        +updateStatus(reportID String, status ReportStatus) Result
    }

    class EventRepository {
        +getEvents(location LatLng, radius Double) List~Event~
        +registerForEvent(eventID String, userID String) Result
        +createEvent(event Event) Result
    }

    User "1" --> "0..*" Report : submits
    User "1" --> "0..*" EventRegistration : registers for
    User "1" --> "0..*" Comment : writes
    User "1" --> "0..*" Notification : receives
    User "1" --> "0..*" Event : organizes
    Report "1" --> "1" ReportStatus : has
    Report "1" --> "0..*" Comment : contains
    Report "1" --> "0..*" Notification : triggers
    Event "1" --> "0..*" EventRegistration : has
    Authority "1" --> "0..*" Report : manages
    AIReportService ..> Report : generates for
    ReportRepository --> Report : manages
    EventRepository --> Event : manages
```

---
