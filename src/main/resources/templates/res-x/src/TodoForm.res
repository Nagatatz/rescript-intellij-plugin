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
        <li key={todo.name}>
          <strong> {Hjsx.string(todo.name)} </strong>
          {switch todo.description {
          | Some(desc) when desc !== "" => <span> {Hjsx.string(" — " ++ desc)} </span>
          | _ => Hjsx.null
          }}
        </li>
      ),
    )}
  </ul>

let onSubmit = Handler.handler.hxPost(
  "/todos",
  ~securityPolicy=ResX.SecurityPolicy.allow,
  ~handler=async ({request, requestController}) => {
    let formData = await request->Request.formData
    let rawName =
      formData->ResX.FormDataHelpers.maybeString("name")->Option.getOr("")
    let rawDescription =
      formData->ResX.FormDataHelpers.maybeString("description")->Option.getOr("")
    switch Validation.parseTodoInput(~name=rawName, ~description=rawDescription) {
    | Ok({name, description}) =>
      todos := todos.contents->Array.concat([{name, description}])
      renderList()
    | Error(msg) =>
      requestController.setStatus(400)
      renderFormWithError(msg)
    }
  },
)
and renderFormWithError = (msg: string) =>
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
    <p style="color:crimson"> {Hjsx.string(msg)} </p>
  </form>

let renderForm = () =>
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
  </form>

@jsx.component
let make = () =>
  <section>
    <h2> {Hjsx.string("Todos")} </h2>
    {renderList()}
    {renderForm()}
  </section>
