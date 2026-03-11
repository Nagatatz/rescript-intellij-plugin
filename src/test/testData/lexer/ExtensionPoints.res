// Extension points and raw JS

// %raw
let add = %raw(`
  function(a, b) {
    return a + b;
  }
`)

// Inline %raw
let _ = %raw(`Date.now()`)

// %re (regular expression)
let dateRe = %re("/(\d{4})-(\d{2})-(\d{2})/")
let emailRe = %re("/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/")
let flagsRe = %re("/hello/gi")

// Region markers
//#region Helper functions

let helper1 = () => ()
let helper2 = () => ()

//#endregion
