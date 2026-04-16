// libsql + Drizzle client. Uses a local SQLite file under ./data/app.db;
// swap the URL for a Turso URL to scale to the cloud.
@module("@libsql/client") external createClient: 'opts => 'client = "createClient"
@module("drizzle-orm/libsql") external drizzle: 'client => 'db = "drizzle"

@val external processEnv: Dict.t<string> = "process.env"

let dbUrl =
  processEnv
  ->Dict.get("DATABASE_URL")
  ->Option.getOr("file:./data/app.db")

let client = createClient({"url": dbUrl})
let db: 'db = drizzle(client)

// Thin wrappers over Drizzle. In production these would live next to the schema.
@send external select: ('db, 'opts) => 'query = "select"
@send external insert: ('db, 'table) => 'insertBuilder = "insert"
@send external values: ('insertBuilder, 'row) => 'insertBuilder = "values"
@send external returning: 'q => promise<array<'row>> = "returning"
@send external from: ('q, 'table) => 'q = "from"
@send external allAsync: 'q => promise<array<'row>> = "all"
