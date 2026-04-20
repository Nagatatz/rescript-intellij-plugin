// Thin fetch wrapper. When no backend is available it falls back to a local
// greeting so the form stays functional out of the box.
type response
@send external json: response => promise<'a> = "json"
@get external ok: response => bool = "ok"
@val external fetch: (string, 'opts) => promise<response> = "fetch"

let greet = async (name: string): string => {
  try {
    let response = await fetch(
      "/api/greet",
      {
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "body": JSON.stringifyAny({"name": name})->Option.getOr("{}"),
      },
    )
    if response->ok {
      let body = await response->json
      body["message"]
    } else {
      `Hello, ${name}! (offline fallback — no backend at /api/greet)`
    }
  } catch {
  | _ => `Hello, ${name}! (offline fallback — fetch failed)`
  }
}