// Bindings over the Workers KV API (subset). The runtime exposes the
// binding on `env.GREETINGS` (see Server.res).
type namespace
type listResult = {keys: array<{"name": string}>}

@send external get: (namespace, string) => promise<Nullable.t<string>> = "get"
@send external put: (namespace, string, string) => promise<unit> = "put"
@send external list: namespace => promise<listResult> = "list"
