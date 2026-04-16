`src/NativeGreeting.res` demonstrates how to call a Kotlin/Swift `NativeModule` from
ReScript through `@module("react-native") @scope("NativeModules")`. The file contains
*bindings only*; the native implementation must be added separately in `android/app/`
or `ios/`. Follow the official React Native guides for the native side:

- Android: https://reactnative.dev/docs/legacy/native-modules-android
- iOS: https://reactnative.dev/docs/legacy/native-modules-ios
