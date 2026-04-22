// Todo form that POSTs via HTMX, runs user input through `Validation.parseTodoInput`
// (implemented with {{validationLib}} — see `src/Validation.res`), and re-renders the
// list on success or the form with inline errors on validation failure.

type todo = {
  name: string,
  description: option<string>,
}

let todos: ref<array<todo>> = ref([])

let listId = "todo-list"
let formId = "todo-form"

let renderList = () =>
  <ul id={listId}>
    {Hjsx.array(
      todos.contents->Array.map(todo =>
        <li>
          <strong> {Hjsx.string(todo.name)} </strong>
          {switch todo.description {
          | Some(desc) when desc !== "" => <span> {Hjsx.string(" — " ++ desc)} </span>
          | _ => Hjsx.null
          }}
        </li>
      ),
    )}
  </ul>

// Forward-declare the renderer so `onSubmit` (which re-renders the form on
// validation failure) and `renderForm` (which wires `hx-post` to `onSubmit`)
// can reference each other. ReScript's `let rec` rejects a function-call RHS
// like `Handler.handler.hxPost(...)`, so a ref cell is the minimal workaround.
let renderFormRef: ref<option<(~error: string=?, unit) => Jsx.element>> = ref(None)

let onSubmit = Handler.handler.hxPost(
  "/todos",
  ~securityPolicy=ResX.SecurityPolicy.allow,
  ~handler=async ({request, requestController}) => {
    let formData = await request->Request.formData
    let rawName =
      formData->ResX.FormDataHelpers.getString("name")->Option.getOr("")
    let rawDescription =
      formData->ResX.FormDataHelpers.getString("description")->Option.getOr("")
    switch Validation.parseTodoInput(~name=rawName, ~description=rawDescription) {
    | Ok({name, description}) =>
      todos := todos.contents->Array.concat([{name, description}])
      renderList()
    | Error(msg) =>
      requestController.setStatus(400)
      switch renderFormRef.contents {
      | Some(render) => render(~error=msg, ())
      | None => Hjsx.null
      }
    }
  },
)

let renderForm = (~error=?, ()) =>
  <form
    id={formId}
    hxPost={onSubmit}
    hxSwap={ResX.Htmx.Swap.make(OuterHTML)}
    hxTarget={ResX.Htmx.Target.make(CssSelector(`#${formId}`))}>
    <label>
      {Hjsx.string("Name ")}
      <input type_="text" name="name" required={true} maxLength=80 />
    </label>
    <label>
      {Hjsx.string("Description ")}
      <input type_="text" name="description" maxLength=240 />
    </label>
    <button type_="submit"> {Hjsx.string("Add todo")} </button>
    {switch error {
    | Some(msg) => <p style={{color: "crimson"}}> {Hjsx.string(msg)} </p>
    | None => Hjsx.null
    }}
  </form>

renderFormRef := Some(renderForm)

@jsx.component
let make = () =>
  <section>
    <h2> {Hjsx.string("Todos")} </h2>
    {renderList()}
    {renderForm()}
  </section>
