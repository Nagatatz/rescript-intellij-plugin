// Minimal bindings over react-native's core components.
module Style = {
  type t
  @obj external make: (
    ~flex: int=?,
    ~padding: int=?,
    ~margin: int=?,
    ~backgroundColor: string=?,
    unit,
  ) => t = ""
}

module View = {
  @module("react-native") @react.component
  external make: (
    ~style: Style.t=?,
    ~children: React.element=?,
  ) => React.element = "View"
}

module Text = {
  @module("react-native") @react.component
  external make: (~children: React.element=?) => React.element = "Text"
}

module TextInput = {
  @module("react-native") @react.component
  external make: (
    ~value: string,
    ~onChangeText: string => unit,
    ~placeholder: string=?,
  ) => React.element = "TextInput"
}

module Button = {
  @module("react-native") @react.component
  external make: (
    ~title: string,
    ~onPress: ReactEvent.Synthetic.t => unit,
  ) => React.element = "Button"
}

module FlatList = {
  @module("react-native") @react.component
  external make: (
    ~data: array<'a>,
    ~keyExtractor: 'a => string,
    ~renderItem: {"item": 'a} => React.element,
  ) => React.element = "FlatList"
}