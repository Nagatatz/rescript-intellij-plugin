The starter keeps the todo list in a process-local `ref`, which is
convenient for exploring HTMX patterns but evaporates on every restart.
Bun ships an embedded SQLite driver — `bun:sqlite` — so promoting the
template to durable storage is a day-two task, not a migration.

1. **Open a database in `Db.res`.** Add a small wrapper that opens
   `data/app.db` and creates the `todos` table if it is missing.

   ```rescript
   type database
   @module("bun:sqlite") external makeDatabase: string => database = "Database"
   @send external run: (database, string) => unit = "run"
   @send external query: (database, string) => 'stmt = "query"

   let db = makeDatabase("data/app.db")

   db->run(
     "CREATE TABLE IF NOT EXISTS todos (" ++
       "id INTEGER PRIMARY KEY AUTOINCREMENT, " ++
       "name TEXT NOT NULL, " ++
       "description TEXT" ++
     ")",
   )
   ```

2. **Replace the `todos` ref in `TodoForm.res` with real queries.** The
   `hx-post` handler runs once per request, so it can call `Db.query(...)`
   directly. Use `@send` to expose `all`, `get`, `run`, and `values` on
   the prepared statement type.

3. **Commit the `data/` folder to `.gitignore`.** The `.gitignore`
   generator already lists `data/` for most templates; keep it excluded
   so the SQLite file stays out of version control.

4. **Back up with a Bun one-liner.** `bun:sqlite` exposes
   `database.serialize()` which returns a `Uint8Array` ready to write to
   disk or upload to object storage.

The HTMX surface does not change: handlers still return JSX fragments
that swap into the `#todo-list` and `#todo-form` elements. Only the
source of truth moves from memory to disk.
