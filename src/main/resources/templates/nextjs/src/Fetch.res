// Tiny fetch wrapper for JSON POSTs. Shared between Client Components.
type response
@send external json: response => promise<'a> = "json"
@val external fetch: (string, 'opts) => promise<response> = "fetch"

let post = (url: string, body: string): promise<response> =>
  fetch(url, {
    "method": "POST",
    "headers": {"Content-Type": "application/json"},
    "body": body,
  })