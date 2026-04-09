# 📚 AI Study Planner — Dynamic Timetable Generator

An Android application for engineering students that **automatically generates and updates a smart timetable** based on tasks, deadlines, and daily study hours.

---

## 🎯 Features

| Feature | Description |
|---|---|
| ➕ Add Tasks | CODING · ASSIGNMENT · PROJECT · EXAM |
| 🤖 Auto Scheduling | Priority-based rule engine assigns daily time slots |
| 🔄 Dynamic Update | Timetable regenerates automatically whenever a task changes |
| 📅 Today's View | Dashboard shows only today's slots |
| 🗓 Full Timetable | Grouped day-by-day view up to 14 days ahead |
| ✅ Complete Tasks | Mark tasks/slots done; schedule adjusts |
| 🔔 Notifications | Deadline reminders via BroadcastReceiver |

---

## 🧠 Scheduling Algorithm

```
Priority Score = (Deadline Urgency × 0.6) + (Task Type Weight × 0.4)

Deadline Urgency : overdue=10, today=9, 1 day=8 … 8+ days=1
Task Type Weight : EXAM=4, ASSIGNMENT=3, CODING=2, PROJECT=1

Stress-free rules
  • Max 3 tasks per day
  • Max 4 hours per day (user-configurable)
  • 30-minute buffer between tasks
  • Coding capped at 1.5 h/session
  • All others capped at 2 h/session
```

---

## 🏗 Architecture

```
UI (Activities)
    │
TaskViewModel  ──► TaskRepository
                        │
              ┌─────────┴──────────┐
        FirebaseRepository    SchedulerEngine
              │
         Firestore DB
         ┌──────────┐
         │  tasks   │
         │ schedule │
         │ settings │
         └──────────┘
```

---

## 🛠 Tech Stack

- **Language** — Kotlin
- **UI** — XML + Material Design 3
- **Architecture** — MVVM (ViewModel + LiveData)
- **Database** — Firebase Firestore (real-time)
- **Async** — Kotlin Coroutines + Flow
- **Notifications** — BroadcastReceiver

---

## 🚀 Setup & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- A Firebase project with Firestore enabled

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/JamiSaiAkshaya/AI-Study-Planner.git
   cd AI-Study-Planner
   ```

2. **Firebase setup**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Open project **ai-study-planner-45b04**
   - Enable **Cloud Firestore** (Start in test mode)
   - The `google-services.json` is already included in `app/`

3. **Open in Android Studio**
   - File → Open → select the cloned folder
   - Wait for Gradle sync to finish

4. **Run**
   - Connect a device or start an emulator (API 24+)
   - Press ▶ Run

---

## 📁 Project Structure

```
app/src/main/java/com/studyplanner/app/
├── data/
│   ├── firebase/       FirebaseRepository.kt
│   ├── models/         Task.kt  ScheduleItem.kt  UserSettings.kt
│   └── repository/     TaskRepository.kt
├── scheduler/          SchedulerEngine.kt
├── ui/
│   ├── activities/     Splash · Dashboard · AddTask · Timetable · AllTasks
│   ├── adapters/       ScheduleAdapter · TimetableAdapter · TaskAdapter
│   └── viewmodel/      TaskViewModel.kt
└── notifications/      DeadlineReceiver.kt
```

---

## 📄 Firestore Collections

| Collection | Purpose |
|---|---|
| `tasks` | One document per task |
| `schedule` | Generated time slots (replaced on every regeneration) |
| `settings` | Single document `user_settings` |

---

## 🔮 Future Scope

- AI/ML-based scheduling optimisation
- Spaced repetition revision algorithm
- Firebase Authentication for multi-user support
- Cloud sync across devices
- Productivity analytics dashboard
