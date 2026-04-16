This template only ships the JavaScript/TypeScript + ReScript surface. The `android/`
and `ios/` projects are generated on first run by the React Native Community CLI.

```bash
{{installCmd}}
{{execReactNative}} init-android
{{execReactNative}} init-ios        # macOS only
```

If `init-android` / `init-ios` are not available in your CLI version, use the latest
upstream template instead:

```bash
npx @react-native-community/cli@latest init tmp
# then copy tmp/android (and tmp/ios) into this project
```

Prerequisites for Android builds: JDK 21, Android Studio, Android SDK, NDK, watchman
(macOS/Linux). For iOS builds: Xcode 16+ and CocoaPods on macOS.
