// Bindings for a custom native module exposed from Android/iOS via
// React Native's NativeModules bridge.
//
// Implement the matching Kotlin/Swift module following the official React
// Native documentation, then call `NativeGreeting.greet("world")` from your
// ReScript code. This file contains *bindings only* — the native side must be
// provided by you.
//
// Docs: https://reactnative.dev/docs/legacy/native-modules-android
module NativeGreeting = {
  @module("react-native") @scope("NativeModules")
  external module_: {"greet": string => promise<string>} = "NativeGreeting"

  let greet = (name: string): promise<string> => module_["greet"](name)
}