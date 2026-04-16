@module("@libsql/client") external createClient: 'opts => 'client = "createClient"
@module("drizzle-orm/libsql") external drizzle: 'client => 'db = "drizzle"

@val external processEnv: Dict.t<string> = "process.env"

let dbUrl =
  processEnv
  ->Dict.get("DATABASE_URL")
  ->Option.getOr("file:./data/app.db")

let client = createClient({"url": dbUrl})
let db: 'db = drizzle(client)

@send external insert: ('db, 'table) => 'builder = "insert"
@send external values: ('builder, 'row) => 'builder = "values"
@send external returning: 'q => promise<array<'row>> = "returning"
@send external select: ('db, 'opts) => 'query = "select"
@send external from: ('q, 'table) => 'q = "from"
@send external allAsync: 'q => promise<array<'row>> = "all"
