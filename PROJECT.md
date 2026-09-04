# DFund - AI-Powered Automated Personal Finance & Financial Awareness App

## 1. Project Overview
DFund is a personal finance and financial awareness application built primarily for ordinary people: low-educated users, gig economy workers, individuals with irregular incomes, non-finance backgrounds, and parents seeking to allocate small surplus amounts wisely.

Rather than acting merely as an expense log or data vault, DFund transforms financial transactions into clear, actionable awareness, proactive budgeting, micro-investment guidance (SIPs, recurring deposits, emergency safety nets), and conversational automation where users can command the app using natural speech in their native languages (English, Tamil, Telugu, Malayalam).

---

## 2. Purpose & Philosophy
- **Action over Inaction**: Don't just show graphs—tell the user what step to take next.
- **Micro-Savings First**: Recognize that even ₹50, ₹100, or ₹500 surplus can grow through disciplined saving and safe investing.
- **Accessible to All**: Plain speak, zero jargon, voice-first interactions for users who struggle with reading or typing.
- **Autonomous In-App Execution**: When the user asks the AI to set a budget, calculate an SIP, or categorize an expense, the app executes the action directly.

---

## 3. Technology Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Mobile OS** | Android (Min SDK 26, Target SDK 34) | Java-based native application |
| **Language** | Java (17/21) | Robust, enterprise-grade Android development |
| **UI Framework** | XML Layouts + Material 3 | High-contrast, accessible, minimalist & premium aesthetic |
| **Architecture** | MVVM + Clean Architecture | Repository pattern, ViewModels, LiveData/Flow, UseCases |
| **Local Database** | Room DB (Java) | Encrypted storage with SQLCipher / Room Entities & DAOs |
| **SMS Analysis** | `Telephony.Sms` / `ContentResolver` | Automatic UPI SMS parsing & transaction extraction |
| **Voice Input / STT**| Sarvam Saaras API | High-accuracy speech-to-text for Indian regional languages |
| **AI Reasoning & Agent**| Sarvam-105B / Reasoning LLM | Intent extraction, personalized financial coaching & tool execution |
| **TTS (Speech Out)** | Local Android TTS / Regional TTS | Audio responses in Tamil, Telugu, Malayalam, English |
| **Financial Engine** | Java Business Logic | SIP, compounding, irregular income smoothing, debt avalanche |
| **Backend API** | Python 3.11+ & FastAPI | Asynchronous REST endpoints, AI orchestration, sync |
| **Backend Database** | PostgreSQL | Relational storage for user profiles, synced ledgers & logs |
| **Networking** | Retrofit 2 + OkHttp 3 | REST communication, offline interceptor, token refresh |
| **Security** | Android Keystore | Hardware-backed key generation, AES-256-GCM encryption |

---

## 4. Folder Structure (Target)

```
d:/Dfund/
├── .graphify/
│   └── knowledge_graph.json
├── PROJECT.md
├── android/                         # Native Android Application (Java)
│   ├── app/
│   │   ├── build.gradle
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── AndroidManifest.xml
│   │   │   │   ├── java/com/dfund/app/
│   │   │   │   │   ├── data/            # Room DB, SMS Repository, Remote API
│   │   │   │   │   ├── di/              # Dependency Injection / Service Locators
│   │   │   │   │   ├── domain/          # Financial Engine, Calculators, Models
│   │   │   │   │   ├── ui/              # Activities, Fragments, ViewModels, Adapters
│   │   │   │   │   └── utils/           # SMS Parser, Encryption, Locale Helpers
│   │   │   │   └── res/
│   │   │   │       ├── layout/          # XML Layouts (Material 3)
│   │   │   │       ├── values/          # Strings (en, ta, te, ml), Colors, Themes
│   │   │   │       └── values-night/
│   │   ├── proguard-rules.pro
│   ├── build.gradle
│   └── settings.gradle
└── backend/                         # FastAPI Service
    ├── app/
    │   ├── api/                     # Endpoints (auth, ai, sync, transactions)
    │   ├── core/                    # Config, database session, security
    │   ├── models/                  # SQLAlchemy ORM models
    │   ├── schemas/                 # Pydantic validation schemas
    │   └── services/                # Sarvam AI orchestrator, financial insights
    ├── main.py
    ├── requirements.txt
    └── Dockerfile
```

---

## 5. Architecture Overview & Finalized Design Decisions
- **UPI Transaction Ingestion**: Hybrid approach using `READ_SMS` / `RECEIVE_SMS` with automatic fallback to `NotificationListenerService` (for real-time GPay/PhonePe/Paytm/BHIM push alerts) and a built-in realistic mock transaction generator for development and testing.
- **AI Action Agent & Tool Dispatch**: FastAPI orchestrates Sarvam AI to output structured tool calls (`ACTION_CALCULATE_SIP`, `ACTION_FILTER_CATEGORY`, `ACTION_ALLOCATE_SURPLUS`, `ACTION_SWITCH_LANGUAGE`, etc.). Android Java ViewModels dispatch actions directly into the UI state.
- **Voice Ingestion & STT**: Android records audio clips (WAV) and streams them to FastAPI `/api/voice/interact`. The backend calls Sarvam Saaras API for accurate Indian regional STT and routes intent in a single round-trip.
- **Speech Feedback (TTS)**: Hybrid audio pipeline: Android native `TextToSpeech` for immediate feedback (<100ms), and Backend Neural TTS for rich financial coaching explanations in Tamil, Telugu, Malayalam, and English.
- **Financial Mental Model**: "Three Buckets + Smart Surplus Radar":
  1. *Safety Shield*: Dynamic rolling buffer calibrated for irregular gig worker income volatility.
  2. *Upcoming Dues*: Clear countdown of upcoming EMIs and recurring utility bills parsed from SMS.
  3. *Growth Pot*: Small surplus allocation visualizer comparing Idle Bank (3%) vs Recurring Deposit (7%) vs Micro-SIP (12%) vs Digital Gold.
- **Local Security**: Android Keystore AES-256-GCM hardware-backed master key, EncryptedSharedPreferences for tokens, and optional Android `BiometricPrompt` security.
- **Backend & Database**: Python + FastAPI with PostgreSQL container (docker-compose) and automatic SQLite fallback for zero-friction local development.
- **UI & Aesthetics (`ui-ux-pro-max`)**: Material 3 Minimalist clean light palette (#FAFAFA), dark mode auto-support, high-contrast semantic chips, large bold rupee values (24-32sp), and prominent Floating Voice FAB.

---

## 6. Roadmap & Implementation Milestones
- [x] **Milestone 1**: Project Scaffolding & Shared Models (Android Java MVVM + FastAPI Backend + Docker Compose).
- [x] **Milestone 2**: Local Database & Security (Room DB + Android Keystore + EncryptedSharedPreferences).
- [x] **Milestone 3**: UPI SMS & Notification Ingestion Engine (Regex-based bank templates: HDFC, SBI, ICICI, Axis, GPay, PhonePe, Paytm).
- [x] **Milestone 4**: Java Financial Calculation Engine (SIP Calculator, Irregular Income Volatility Buffer, 3-Way Surplus Growth Comparator).
- [x] **Milestone 5**: FastAPI Backend & AI Orchestration (Sarvam Saaras STT proxy, Sarvam AI Tool Dispatcher, PostgreSQL schemas).
- [x] **Milestone 6**: Accessible Material 3 UI & Multilingual Localization (XML Layouts, Strings for EN, TA, TE, ML, Voice FAB).
- [x] **Milestone 7**: AI Voice Action Navigation Loop & Speech Synthesizer (End-to-End Voice Command -> Action Execution).
- [x] **Milestone 8**: Comprehensive Verification & Testing (Android Unit Tests, Backend Pytest, Mock Data Flow).
- [x] **Milestone 9**: Android APK Compilation & Physical Device Deployment (`app-debug.apk` built and installed on live mobile device).
- [x] **Milestone 10**: Live Device Fixes & Speech Recognition (Resolved 0 spend via Inbox Scanner + auto-seed, integrated native `SpeechRecognizer` with `LocalAiIntentParser` on-device fallback, 100% verified with device screencaps).
- [x] **Milestone 11**: Attractive White/Light Theme Redesign (Toss / Apple Wallet fintech aesthetic, enforced light mode, crisp white elevated cards, pastel status badges, and refined typography verified on device).
- [x] **Milestone 12**: Automated 1-Click Scripts & System Path Configuration (`build_apk.bat`, `run_on_phone.bat`, `start_backend.bat` in root, `adb` added to User PATH).
- [x] **Milestone 13**: Automatic SMS Ingestion on Open & Sarvam-Tamil-Ollama (Minimax-M3) Pipeline:
  - Automatic SMS scanning & deduplication on app open (`onCreate` / `onResume`).
  - Sarvam Saaras STT hears the voice and detects language.
  - Sarvam Translate translates user prompt into Tamil (`ta-IN`).
  - Ollama Cloud (`minimax-m3:cloud`) analyzes user financial ledger & Tamil query to deliver tailored financial advice and in-app action codes.
  - Sarvam Bulbul v3 TTS synthesizes voice response in the user's language (`ta-IN`, `en-IN`, `te-IN`, `ml-IN`).
  - Interactive UI with AI Financial Advisor card on dashboard and in voice dialog with one-tap action trigger.
- [x] **Milestone 15**: Automatic Action Execution & Spoken Result Announcement:
  - Eliminated manual "Execute Action" confirmation button: When user speaks or asks via voice, the app executes the action autonomously without requiring extra permission taps.
  - Brief 1.2s card preview with auto-transition ensures the user sees the advice summary and immediately lands on the target screen.
  - Voice assistant explicitly communicates:
    1. **What was done** (e.g. calculated SIP, filtered transactions, checked upcoming EMIs).
    2. **Exact numerical result** (e.g. Total invested ₹18,000, expected maturity ₹22,812 with ₹4,812 profit; total due is ₹4,100; spent ₹7,198).
  - Synchronized bottom navigation menu tabs during programmatic transitions.
- [x] **Milestone 16**: Sarvam Full Indian Regional AI Suite (Sarvam 105B Conversations LLM, Saaras v4 STT, Mayura v1 Translation, Bulbul v3 TTS):
  - Upgraded financial reasoning and tool dispatch engine entirely to **`sarvam-105b-conversations`** (Sarvam 105B parameter Indian-context LLM).
  - Upgraded Speech-to-Text to **`saaras:v4`** with 23 Indian languages and automatic language detection.
  - Upgraded translation to **`mayura:v1`** with tone and script control.
  - Upgraded neural voice synthesis to **`bulbul:v3`** (48 kHz HD).
  - Completely bypassed Ollama to run an all-in-one native Indian AI stack.
  - Verified live: Spoken query translated to Tamil -> analyzed by `sarvam-105b-conversations` -> produced explicit what was done + exact numerical results -> dispatched in-app action payload -> synthesized Bulbul v3 neural voice speech.

- [x] **Milestone 18**: High-Fidelity UI Match (8 Reference Images), 7-Step Onboarding & Complete Clean Architecture:
  - **Pixel-Perfect Alignment with Reference UI Images**:
    - Re-architected app navigation into a 3-tab Core shell: **Home** (matching Reference Image 8), **Savings** (matching Reference Image 7), and **Profile** (Fintech Card UI with Log Out).
    - **Home / Dashboard**: Pine Green theme (#0A4D3C), Mint Card (#F4FBF7 / #D1FAE5) with real-time Spending vs Limit ratio (e.g. 18% used), 2x2 Quick Actions grid (Add Income, Add Expense, Emergency Fund, Save & Invest), motivational savings banner ("Small steps, big change!"), live Sarvam 105B AI Advisor with audio TTS playback, and Floating Voice FAB ("Tap to speak").
    - **Savings & Investments**: Total Saved hero card with monthly/previous comparison pills, Target Savings progress bar, dynamic potential monthly savings breakdown ("Your income is ₹X, essentials total ₹Y, after buffer ₹Z, you have ₹W for savings"), 3-Way Cashflow buckets (Essentials, Spending, Surplus/SIP), and active investment cards (Index Nifty 50 SIP, Bank Recurring Deposit).
  - **Comprehensive 7-Step Onboarding Flow**:
    1. **Language Selection** (Step 1): English, Hindi, Tamil, Telugu with native script headers and dynamic locale activation.
    2. **Authentication** (Step 2 - Image 2): 10-digit Phone Number, optional PAN card for tax saving/KYC, OTP verification mock, bank-grade encryption badge.
    3. **Automated SMS Permission** (Step 3 - Image 3): Transparent privacy explanation ("We only read bank transaction SMS alerts; we NEVER read personal texts or OTPs").
    4. **Work & Income Profile** (Step 4 - Image 4): Multi-selection of work types (Gig Worker, Salaried, Daily Wage, Self-Employed, Homemaker, Other) with dynamic monthly income slider and volatility indicator.
    5. **Mandatory Monthly Expenses** (Step 5 - Image 5): Interactive expense cards (Rent, Groceries/Food, Loan EMI, Utilities/Bills, Transport, Other) with instant sum calculation and income vs expense sanity guard.
    6. **Savings Preferences & Goals** (Step 6 - Image 6): Priority selection (Emergency Fund, Safe Fixed Deposit, Mutual Fund SIP, Child Education, Medical Cushion) and target monthly savings percentage slider.
    7. **Personalized Money Plan** (Step 7): AI-generated plan calculating Safe Daily Limit, Volatility Cushion, and Surplus Pot with 1-tap "Go to Dashboard" transition.
  - **Full Security & Session Logout**:
    - Profile screen equipped with user session management, live cushion adjusters (+/-), risk appetite chip selector, theme toggle, and styled destructive red "Log Out" card.
    - Logout confirmation dialog clears session keys and resets onboarding state, safely returning to Step 1 without app restarts.
  - **Sarvam 105B AI Suite Integration**:
    - Direct calls to `sarvam-105b` / `sarvam-105b-conversations` for financial advice and action routing.
    - `saaras:v4` STT for 23 Indian regional languages.
    - `mayura:v1` translation for high-fidelity regional language conversions.
    - `bulbul:v3` 48 kHz neural TTS for voice playback.
  - **Zero Overlap UI Geometry**:
    - Applied `clipToPadding="false"` and `paddingBottom="160dp"` with trailing spacers on all scrollable screens, ensuring all interactive buttons and cards scroll completely clear of the floating voice mic and bottom navigation bar.

---

## 7. Current Status
- **Phase**: Milestone 18 Complete — Full 7-Step Onboarding, 3-Tab High-Fidelity UI Match, Profile & Secure Logout, and Sarvam 105B AI Suite fully verified on Android 15 emulator and physical device.
- **APK Location**: `d:\Dfund\DFund-app-debug.apk` in project root and `d:\Dfund\android\app\build\outputs\apk\debug\app-debug.apk`.
- **Backend Status**: Online at `http://0.0.0.0:8000` with Sarvam 105B AI integration, profile endpoints, and health monitoring.
- **Testing & Verification**: 100% verified across all 7 onboarding steps, Dashboard, Savings/Investments, Profile editing, and Logout cycle.
- **Memory**: Graphify Knowledge Graph synchronized.




