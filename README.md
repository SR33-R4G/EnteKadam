# എൻ്റെ കടം (Ente Kadam)

Offline-first Android personal debt manager built with Kotlin, Jetpack Compose, Material 3 and Room.

## Project details
- Package: `in.entekadam.app`
- Minimum Android: 8.0 (API 26)
- Compile / target SDK: 35
- JDK: 17
- Default language: Malayalam; English is available in Settings
- Local Room database: `ente_kadam.db`

## Open and build
1. Install Android Studio (Ladybug or newer) and Android SDK Platform 35.
2. Open this folder (`EnteKadam`) in Android Studio.
3. Allow Gradle sync and install the Android SDK components if prompted.
4. Run the `app` configuration on an Android 8.0+ device or emulator.
5. For an APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

Command line build (with Android SDK and Gradle available):

```bash
gradle assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Data and financial model
- Amounts are stored as integer paise (`Long`), avoiding floating-point currency arithmetic.
- A person belongs to one account direction: `RECEIVE` (they owe you) or `PAY` (you owe them).
- Each loan/additional advance is a separate debt row.
- Every repayment is an append-only payment row; payments are not overwritten when new repayments are entered.
- Outstanding balance is calculated as debt principal less recorded repayments. Optional interest is stored as a note/metadata rate; interest is not compounded or automatically added to the principal.
- Deleting is intentionally conservative: financial rows are not hard-deleted from the UI. Audit records are stored for debt/payment creation.

## Backup
Settings → Create JSON backup exports people, debts, payments and audit rows using Android's document picker. Restore validates the file and requires confirmation before replacing local data. Keep backup files private; the JSON is not encrypted.

## Sharing and reports
- Person details offers a preview/editable account statement and Android share sheet / WhatsApp target.
- Reports can be exported to PDF and shared through Android's share sheet.

## Reminders
Entering a due date schedules a local reminder for 9:00 AM on that date when the date is in the future. Android notification permission may need to be enabled in system settings on Android 13+.

## Privacy
No login, cloud service, analytics SDK or internet permission is declared. Records remain in the app's local Room database. Android backup is disabled in the manifest.

## Important implementation notes
This is a source project, not a signed release. A release APK must be built and signed with the owner's keystore. The project has not been compiled in this environment because an Android SDK / Gradle installation is not present. Please run a Gradle sync/build in Android Studio before production use. App lock/biometric gating, full edit/archive workflows, custom date-range filters, monthly chart visualizations, and reminder rescheduling after device reboot are not included in this initial source drop.

## Build APK using GitHub (no Android Studio required)

1. Create a new GitHub repository (for example, `EnteKadam`).
2. Extract this ZIP and upload the **contents of the `EnteKadam` folder** to the repository root. Keep the `.github/workflows/android.yml` file and its folders; enable showing hidden files if needed.
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab and select **Build Ente Kadam APK**.
5. Choose **Run workflow** if it has not started automatically, then wait for the build to finish.
6. Open the successful workflow run, scroll to **Artifacts**, and download `Ente-Kadam-debug-apk`.
7. Extract the downloaded artifact ZIP and install `app-debug.apk` on your Android phone. Android may ask you to allow installation from that browser/file manager.

The workflow runs the unit tests and creates a debug APK. It does not create a signed Play Store release. GitHub Actions requires internet access during the cloud build to download Gradle and Android dependencies; the installed app itself is designed to store its records locally and work offline.
