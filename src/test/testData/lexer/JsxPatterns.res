// JSX patterns for lexer testing

// Self-closing element
let a = <br />
let b = <input type_="text" />
let c = <Component prop1="value" prop2={42} />

// Element with children
let d = <div>
  <span> {React.string("hello")} </span>
</div>

// Fragment
let e = <>
  <span />
  <span />
</>

// Nested components
let f = <Outer>
  <Inner>
    <div> {React.string("deep")} </div>
  </Inner>
</Outer>

// JSX with spread props
let g = <Component {...props} />

// JSX with optional prop
let h = <Component ?onChange />

// JSX with punned prop
let i = <Component className />

// JSX in expression position
let j = switch x {
| true => <div> {React.string("yes")} </div>
| false => <span />
}

// Module component
let k = <Mod.Component />
let l = <Mod.Sub.Component> {React.string("hi")} </Mod.Sub.Component>

// Component with key
let m = <Component key="unique" />

// List of JSX
let n = list{
  <div key="1" />,
  <div key="2" />,
  <div key="3" />,
}

// JSX with conditional
let o = <div>
  {showTitle ? <h1> {React.string(title)} </h1> : React.null}
</div>
