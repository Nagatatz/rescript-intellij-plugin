- **Metro cannot reach the device:** run `adb reverse tcp:8081 tcp:8081` after
  connecting the device.
- **Stale Metro cache:** restart Metro with `{{cmdStart}} --reset-cache`.
- **Gradle errors after upgrading deps:** run `cd android && ./gradlew clean` and
  rebuild from Android Studio.
- **Module not found for `.res.mjs`:** confirm `metro.config.js` still contains `mjs`
  in `resolver.sourceExts`.
