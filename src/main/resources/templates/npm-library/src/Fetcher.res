// Wraps the global `fetch` with a timeout; demonstrates Promise + AbortController.
@val external fetch: (string, 'opts) => promise<'response> = "fetch"

type abortController
@new external makeAbortController: unit => abortController = "AbortController"
@get external signal: abortController => 'signal = "signal"
@send external abort: abortController => unit = "abort"

@val external setTimeout: (unit => unit, int) => 'timer = "setTimeout"
@val external clearTimeout: 'timer => unit = "clearTimeout"

/**
 * Fetches [url] and rejects with a RangeError-like exception if the request
 * takes longer than [timeoutMs] milliseconds.
 */
@genType
let fetchWithTimeout = async (url: string, ~timeoutMs: int): promise<'response> => {
  let controller = makeAbortController()
  let timer = setTimeout(() => controller->abort, timeoutMs)
  try {
    let response = await fetch(url, {"signal": controller->signal})
    clearTimeout(timer)
    response
  } catch {
  | err =>
    clearTimeout(timer)
    raise(err)
  }
}