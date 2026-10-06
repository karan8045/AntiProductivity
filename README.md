# AntiProductivity 🛑📱

AntiProductivity is an aggressive, no-nonsense anti-procrastination Android launcher and app blocker designed to actively stop mindless doomscrolling and procrastination.

---

## 🚀 Features

- **Custom Android Launcher:** Replaces your default home screen with a minimal, dark, high-contrast, intimidating interface ("WHAT ARE YOU DOING? GO BUILD SOMETHING.").
- **Foreground App Detection:** Uses an Accessibility Service to detect when you attempt to open a blocked distracting app (e.g. YouTube, Instagram, Facebook, games).
- **Random String Challenge:** Forces you to successfully complete a case-sensitive random string typing challenge before unlocking the app.
- **15-Minute Temporary Unlock:** Earning your procrastination gives you a temporary 15-minute unlock session. After 15 minutes, blocking automatically resumes.
- **Humorous Roasts:** If you fail a challenge, you get roasted with witty productivity messages.
- **Local Statistics:** Tracks blocked attempts, successful challenges, failed challenges, active unlock sessions, minutes unlocked, and daily streaks locally.
- **Privacy-First:** Zero cloud backend, zero analytics, zero data collection, zero ads.

---

## 🛠️ How It Works

1. **Launcher Integration:** Set AntiProductivity as your device's default home launcher (`ACTION_MAIN`, `CATEGORY_HOME`).
2. **Accessibility Access:** Enable the AntiProductivity Accessibility Service in Android Settings.
3. **App Blocking:** Select which installed applications are considered distracting in the Blocked Apps menu.
4. **Interception:** When you open a blocked app, the Accessibility Service intercepts the event, checks for an active unlock session, and launches the full-screen Challenge Activity if locked.
5. **Challenge & Earn:** Type the exact random verification string correctly to earn a 15-minute temporary unlock.

---

## 🔒 Required Android Permissions & Privacy

- **Default Home Launcher:** Allows AntiProductivity to act as your primary home screen.
- **Accessibility Service (`BIND_ACCESSIBILITY_SERVICE`):** Used **strictly** to detect the foreground application package name to trigger the blocking challenge. It does **not** read private messages, capture passwords, record keystrokes, or upload personal data anywhere.
- **Privacy Guarantee:** All settings, unlock sessions, and statistics are stored locally on your device using Jetpack DataStore.

---

## ⚙️ Setup Instructions

1. **Install the APK** on an Android device (API 26 or higher).
2. Open AntiProductivity for the first time to go through the **Onboarding Setup**:
   - Set AntiProductivity as your Default Home Launcher.
   - Enable Accessibility access.
   - Select your distracting apps.
3. Enjoy your distraction-free phone!

---

## 🏗️ How to Build

1. Clone or open the project in **Android Studio** (Koala / Ladybug or newer with Kotlin 2.2+).
2. Run Gradle Sync.
3. Run `app:assembleDebug` or deploy directly to a connected physical device or emulator.
