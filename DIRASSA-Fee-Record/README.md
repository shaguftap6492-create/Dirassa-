# DIRASSA Fee Record — Android App

**Offline Student Fee Management App for DIRASSA CLASSES**

---

## 📱 App Overview

A fully offline Android app (Android 8.0+) for managing student fees at DIRASSA CLASSES coaching institute. All data is stored locally on the device using Room (SQLite). No internet connection required.

---

## 🏗️ Project Structure

```
DIRASSA-Fee-Record/
├── app/
│   ├── build.gradle                          # App-level dependencies
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/dirassa/feerecord/
│       │   ├── FeeRecordApplication.kt       # App singleton, DI roots
│       │   ├── data/
│       │   │   ├── entity/
│       │   │   │   ├── Student.kt            # Room entity
│       │   │   │   ├── FeeRecord.kt          # Room entity
│       │   │   │   └── StudentWithFee.kt     # Query result model
│       │   │   ├── dao/
│       │   │   │   ├── StudentDao.kt         # DB queries for students
│       │   │   │   └── FeeRecordDao.kt       # DB queries for fees
│       │   │   ├── database/
│       │   │   │   └── AppDatabase.kt        # Room database singleton
│       │   │   └── repository/
│       │   │       ├── StudentRepository.kt  # Business logic layer
│       │   │       └── FeeRecordRepository.kt
│       │   ├── ui/
│       │   │   ├── viewmodel/
│       │   │   │   ├── StudentViewModel.kt
│       │   │   │   ├── FeeRecordViewModel.kt
│       │   │   │   └── ReportViewModel.kt
│       │   │   ├── splash/SplashActivity.kt
│       │   │   ├── home/HomeActivity.kt
│       │   │   ├── student/
│       │   │   │   ├── AddEditStudentActivity.kt
│       │   │   │   └── StudentListActivity.kt
│       │   │   ├── fee/FeeRecordActivity.kt
│       │   │   ├── report/MonthlyReportActivity.kt
│       │   │   ├── settings/SettingsActivity.kt
│       │   │   └── adapter/
│       │   │       ├── StudentAdapter.kt
│       │   │       ├── FeeHistoryAdapter.kt
│       │   │       └── ReportAdapter.kt
│       │   └── util/
│       │       ├── PdfHelper.kt              # PDF generation (iText)
│       │       ├── ExcelHelper.kt            # Excel export (Apache POI)
│       │       └── BackupHelper.kt           # DB backup/restore
│       └── res/
│           ├── layout/                       # All screen layouts
│           ├── values/                       # Colors, strings, themes
│           ├── drawable/                     # Icons, backgrounds
│           ├── anim/                         # Screen animations
│           └── mipmap-*/                     # Launcher icons
└── build.gradle                              # Top-level gradle config
```

---

## 🚀 How to Build in Android Studio

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

1. **Open the project**
   - Launch Android Studio
   - File → Open → Select the `DIRASSA-Fee-Record` folder

2. **Sync Gradle**
   - Click "Sync Now" when prompted, or go to:
   - File → Sync Project with Gradle Files

3. **Build & Run**
   - Connect an Android device (Android 8.0+) or start an emulator
   - Click the ▶ Run button, or press `Shift+F10`

4. **Generate APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - APK is at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9 |
| UI | Material Design 3, ViewBinding |
| Database | Room (SQLite), LiveData |
| Architecture | MVVM + Repository pattern |
| Async | Kotlin Coroutines |
| PDF | iText 5 (iTextG for Android) |
| Excel | Apache POI |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 34 (Android 14) |

---

## 🗄️ Database Schema

### students
| Column | Type | Notes |
|--------|------|-------|
| student_id | INTEGER PK AUTOINCREMENT | e.g. 1 → displays as "STU001" |
| student_name | TEXT | Required |
| father_name | TEXT | |
| class_name | TEXT | |
| mobile | TEXT | 10 digits |
| monthly_fee | REAL | Required, numeric |
| admission_date | TEXT | dd/MM/yyyy |

### fee_records
| Column | Type | Notes |
|--------|------|-------|
| record_id | INTEGER PK AUTOINCREMENT | |
| student_id | INTEGER FK → students | Cascades on delete |
| month | TEXT | "January"…"December" |
| year | INTEGER | e.g. 2024 |
| amount_paid | REAL | |
| status | TEXT | "Paid" or "Pending" |
| payment_date | TEXT | dd/MM/yyyy |
| remarks | TEXT | |

**Unique constraint**: (student_id, month, year) — prevents duplicate entries

---

## 🖥️ Screens

| Screen | Purpose |
|--------|---------|
| Splash | Animated logo screen, 2.5s delay |
| Home | 4 navigation cards |
| Add/Edit Student | Form with validation + date picker |
| Student List | Searchable list with edit on tap |
| Fee Record | Enter/update/delete fee + payment history |
| Monthly Report | Dashboard cards + filterable list + PDF/share |
| Settings | Backup, restore, export Excel/PDF, reset, dark mode |

---

## ✨ Key Features

- ✅ **Fully Offline** — No internet needed, SQLite via Room
- ✅ **MVVM Architecture** — Clean separation of concerns
- ✅ **Auto Student ID** — STU001, STU002… auto-assigned
- ✅ **Duplicate Prevention** — One record per student per month
- ✅ **PDF Receipt** — Per-student payment receipt
- ✅ **Monthly Report PDF** — Shareable via any app
- ✅ **Excel Export** — Students + records in .xlsx
- ✅ **DB Backup/Restore** — Save and restore .db file
- ✅ **Dark Mode** — System and manual toggle
- ✅ **Hindi + English** — Bilingual labels
- ✅ **Smooth animations** — Slide + fade transitions
- ✅ **Search** — Students searchable by name/class

---

## 🔧 Customization

### Change app colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="primary">#1565C0</color>  <!-- Change to any hex color -->
```

### Add more student fields
1. Add column in `Student.kt` entity
2. Update `StudentDao.kt` queries
3. Add field to `activity_add_edit_student.xml`
4. Update `AddEditStudentActivity.kt` save logic
5. Increment Room database version in `AppDatabase.kt`

### Change institute name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_title">YOUR INSTITUTE NAME</string>
```

---

## 📄 License

This project is built for DIRASSA CLASSES. All rights reserved.
