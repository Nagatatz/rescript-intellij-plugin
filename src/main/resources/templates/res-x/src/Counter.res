// Server-side counter whose value lives in a Bun process-local ref.
// Increment / decrement buttons POST to HTMX endpoints that return the
// refreshed `<span>`; `hx-swap="outerHTML"` on the buttons' target does the
// DOM replacement without a full page reload.

let count = ref(0)

let counterId = "counter-value"

@jsx.component
let make = () => {
  let current = count.contents

  let onIncrement = Handler.handler.hxPost(
    "/counter/increment",
    ~securityPolicy=ResX.SecurityPolicy.allow,
    ~handler=async _ => {
      count := count.contents + 1
      <span id={counterId}> {Hjsx.string(Int.toString(count.contents))} </span>
    },
  )

  let onDecrement = Handler.handler.hxPost(
    "/counter/decrement",
    ~securityPolicy=ResX.SecurityPolicy.allow,
    ~handler=async _ => {
      count := count.contents - 1
      <span id={counterId}> {Hjsx.string(Int.toString(count.contents))} </span>
    },
  )

  <section>
    <h2> {Hjsx.string("Counter")} </h2>
    <p>
      {Hjsx.string("Current value: ")}
      <span id={counterId}> {Hjsx.string(Int.toString(current))} </span>
    </p>
    <button
      type_="button"
      hxPost={onDecrement}
      hxSwap={ResX.Htmx.Swap.make(OuterHTML)}
      hxTarget={ResX.Htmx.Target.make(CssSelector(`#${counterId}`))}>
      {Hjsx.string("-")}
    </button>
    <button
      type_="button"
      hxPost={onIncrement}
      hxSwap={ResX.Htmx.Swap.make(OuterHTML)}
      hxTarget={ResX.Htmx.Target.make(CssSelector(`#${counterId}`))}>
      {Hjsx.string("+")}
    </button>
  </section>
}
