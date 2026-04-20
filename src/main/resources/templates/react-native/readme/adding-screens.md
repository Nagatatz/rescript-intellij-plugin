The template ships as a single screen. For navigation, install `expo-router` or
`@react-navigation/native` and add screens as additional ReScript components
annotated with `@genType @react.component`. Keep shared types in a `Shared.res`
module so the navigator type-checks against the screen's props.