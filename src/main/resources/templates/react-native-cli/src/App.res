// Interactive todo list: useState + FlatList + TextInput + Button.
// Draft input is validated via `Validation.parseDraftTodo` before being
// appended to the list, so blank / too-long entries surface as a banner
// instead of silently joining the todos.
type todo = {id: int, text: string}

@genType @react.component
let make = () => {
  let (todos, setTodos) = React.useState(() => [
    {id: 1, text: "Build {{projectName}}"},
    {id: 2, text: "Ship to the stores"},
  ])
  let (draft, setDraft) = React.useState(() => "")
  let (error, setError) = React.useState(() => None)

  let addTodo = () => {
    switch Validation.parseDraftTodo(draft) {
    | Error(message) => setError(_ => Some(message))
    | Ok({text}) =>
      let nextId =
        todos->Array.reduce(0, (max, t) => t.id > max ? t.id : max) + 1
      setTodos(prev => prev->Array.concat([{id: nextId, text}]))
      setDraft(_ => "")
      setError(_ => None)
    }
  }

  let renderItem = (item: todo) =>
    <ReactNative.View
      key={item.id->Int.toString}
      style={ReactNative.Style.make(~padding=8, ())}>
      <ReactNative.Text> {React.string(item.text)} </ReactNative.Text>
    </ReactNative.View>

  <ReactNative.View style={ReactNative.Style.make(~flex=1, ~padding=24, ())}>
    <ReactNative.Text> {React.string("{{projectName}} TODOs")} </ReactNative.Text>
    <ReactNative.TextInput
      value={draft}
      onChangeText={t => setDraft(_ => t)}
      placeholder="New todo"
    />
    <ReactNative.Button title="Add" onPress={_ => addTodo()} />
    {switch error {
    | Some(message) =>
      <ReactNative.Text> {React.string(`⚠ ${message}`)} </ReactNative.Text>
    | None => React.null
    }}
    <ReactNative.FlatList
      data={todos}
      keyExtractor={item => item.id->Int.toString}
      renderItem={info => renderItem(info["item"])}
    />
  </ReactNative.View>
}