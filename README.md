# Focus Notes (Android)

An Android port of the **Focus Notes** iOS app — a task manager with a built-in
focus timer (elapsed stopwatch + Pomodoro), written in Kotlin with Jetpack
Compose, Material 3, and Room.

## Features (parity with the iOS app)

- **Tasks** — quick-add bar, task list with status (To Do / In Progress / Done),
  due dates, and tracked time. Swipe right to complete, swipe left to delete,
  long-press for a context menu (Focus / Done).
- **Focus** — pinned tasks with an active timer card on top and per-task
  play/pause controls. Sorted active → in-progress → recently updated.
- **Today** — tasks due on or before today that aren't done yet.
- **Timer** — two modes:
  - *Elapsed*: counts up.
  - *Pomodoro*: 25/5 work-break cycles with a progress ring.
  Timer state is wall-clock based, so it survives the app being closed.
- **Notifications** on Pomodoro phase changes and subtle haptic feedback.
- **Local persistence** with Room (`TaskEntity`, `TimerSessionEntity`).

## Architecture

| iOS (SwiftUI / SwiftData) | Android (Compose / Room)            |
| ------------------------- | ----------------------------------- |
| `TaskItem` `@Model`       | `TaskEntity` (Room)                 |
| `TimerSession` `@Model`   | `TimerSessionEntity` (Room)         |
| `TimerEngine` singleton   | `TasksViewModel` + `TimerCalc`      |
| `NotificationService`     | `NotificationService` (NotificationCompat) |
| `@AppStorage` settings    | `PomodoroSettings` (SharedPreferences) |
| SwiftUI Views             | Jetpack Compose screens             |

## Build

CI builds a debug APK on every push to `main` (see
`.github/workflows/build-apk.yml`). Download it from the run's **Artifacts**
or from the **Releases** page.

Locally (needs JDK 17 + Android SDK):

```bash
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

## Install on a phone

1. Download `app-debug.apk` to the phone.
2. Open it; if prompted, allow your browser/file manager to install unknown apps.
3. Tap **Install**.

This is an unsigned debug build — fine for personal use/sideloading.
