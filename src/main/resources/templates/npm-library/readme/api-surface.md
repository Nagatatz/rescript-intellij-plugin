| Module | Export | Description |
| --- | --- | --- |
| `Index.res` | `greet(name)` | Returns a greeting string (sync, `@genType`) |
| `Fetcher.res` | `fetchWithTimeout(url, ~timeoutMs)` | Fetch that rejects after a timeout |
| `ListUtils.res` | `chunk(xs, ~size)` | Splits a list into fixed-size chunks |
| `ListUtils.res` | `partitionMap(xs, f)` | Partitions by Result into `(ok, err)` |