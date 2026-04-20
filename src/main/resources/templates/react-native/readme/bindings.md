`src/ReactNative.res` wraps the core components used by `App.res`: `View`, `Text`,
`TextInput`, `Button`, and `FlatList`. To add more (e.g. `ScrollView`, `Image`,
`Pressable`), follow the same `@module("react-native") @react.component` pattern.
For third-party modules (e.g. `react-native-reanimated`), bind against the package
name instead of `"react-native"`.