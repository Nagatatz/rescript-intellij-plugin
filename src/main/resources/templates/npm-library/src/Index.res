/** Returns a greeting addressed to [name]. */
@genType
let greet = (name: string) => {
  `Hello from {{projectName}}, ${name}!`
}

/** Exposed helpers for JS/TS consumers. */
@genType
let chunk = ListUtils.chunk

@genType
let fetchWithTimeout = Fetcher.fetchWithTimeout