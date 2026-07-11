# 🌿 Eco Guardian

An Android application for reporting and managing trash in public spaces. Users can upload photos, get AI-generated reports, and submit them to admins who coordinate cleanup efforts.

---

## 📱 Features

### User
- Register and log in securely
- Upload a photo from gallery or camera
- AI-generated trash report via Gemini 2.5 Flash
- Edit the report before submitting
- Attach a location link (Google Maps or OpenStreetMap)
- Auto-detect current GPS location
- View submitted reports in Pending and Finished tabs

### Admin
- View all submitted reports from all users
- See photo, report text, and location for each report
- Mark reports as Finished (moves to user's Finished tab)
- TODO: Delete reports
- Auto-refresh every 30 seconds

---

## 🛠️ Tech Stack

| Layer | Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Backend | Supabase (Auth, Database, Storage) |
| AI | Gemini 2.5 Flash API |
| Networking | Ktor Client |
| Image Loading | Coil |
| Serialization | kotlinx-serialization |
| Navigation | Jetpack Navigation Compose |

---

## 🗄️ Supabase Schema

### `profiles` table
| Column | Type | Notes |
|---|---|---|
| id | uuid | References auth.users |
| role | text | `user` or `admin` |

### `reports` table
| Column | Type | Notes |
|---|---|---|
| id | int | Primary key |
| user_id | uuid | References auth.users |
| photo_url | text | Supabase Storage URL |
| report_text | text | AI-generated, editable |
| location_link | text | Google Maps or OSM link |
| status | text | `pending` or `finished` |
| created_at | timestamp | Auto-generated |

---

## ⚙️ Setup

### 1. Clone the repository
```bash
git clone https://github.com/yousef-MAjeeb/Eco-Guardian.git
cd Eco-Guardian/Eco-GuardianApp
```

### 2. Open in Android Studio
File → Open → select the `Eco-GuardianApp` folder

### 3. Add your API keys
Create or edit `local.properties` in the project root and add:
```
SUPABASE_URL=your_supabase_project_url
SUPABASE_KEY=your_supabase_anon_key
GEMINI_API_KEY=your_gemini_api_key
```

- Get Supabase credentials from [supabase.com](https://supabase.com) → your project → Settings → API
- Get Gemini API key from [aistudio.google.com](https://aistudio.google.com)

### 4. Build and run
Connect your Android device or start an emulator → press ▶ Run

---

## 👤 Test Accounts

| Role | Email               | Password        |
|---|---------------------|-----------------|
| User | user5@test.com      | user5@test      |
| Admin | testAdmin@admin.com | testAdmin@admin |

---

## 📁 Project Structure

```
com.ecoguardian/
├── data/
│   ├── AuthRepository.kt
│   ├── GeminiService.kt
│   ├── Report.kt
│   ├── ReportRepository.kt
│   └── SupabaseClient.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── Routes.kt
├── ui/
│   ├── screens/
│   │   ├── AdminPanelScreen.kt
│   │   ├── AiReportScreen.kt
│   │   ├── LoginScreen.kt
│   │   └── UserHomeScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── viewmodel/
    ├── AdminViewModel.kt
    ├── AuthViewModel.kt
    ├── ReportViewModel.kt
    └── UserReportsViewModel.kt
```

---

## 🔑 Environment Variables

> ⚠️ Never commit `local.properties` to version control. It is already listed in `.gitignore`.

---

## 📄 License

This project was built as a team academic project.