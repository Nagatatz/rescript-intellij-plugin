// String edge cases for lexer testing

// Empty strings
let empty = ""
let emptyTemplate = ``

// Escape sequences
let escapes = "hello\nworld\t\r\\\""
let unicode = "\u0041\u{1F600}"

// Template literals
let simple = `hello`
let interpolated = `${name} is ${age->Int.toString} years old`
let nested = `outer ${`inner ${x}`}`
let multiline = `
  line 1
  line 2
  line 3
`

// Template with expressions
let expr = `result: ${1 + 2->Int.toString}`
let withFunc = `${Array.map(arr, x => x + 1)->Array.joinWith(", ")}`

// Strings with special characters
let special = "tab\there"
let raw = "backslash: \\"
let quotes = "she said \"hi\""

// Multiline string
let multi = "this is a \
  continued line"

// Character literals
let ch1 = 'a'
let ch2 = '\n'
let ch3 = '\\'
