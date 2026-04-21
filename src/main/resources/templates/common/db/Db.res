// libsql + Drizzle client. Uses a local SQLite file under ./data/app.db;
// swap the URL for a Turso libsql:// URL to scale to the cloud.
@module("@libsql/client") external createClient: 'opts => 'client = "createClient"
@module("drizzle-orm/libsql") external drizzle: 'client => 'db = "drizzle"

@val external processEnv: Dict.t<string> = "process.env"

let dbUrl =
  processEnv
  ->Dict.get("DATABASE_URL")
  ->Option.getOr("file:./data/app.db")

let client = createClient({"url": dbUrl})
let db: 'db = drizzle(client)

// --- Query chain constructors ------------------------------------------------
@send external select: ('db, 'opts) => 'query = "select"
@send external from: ('q, 'table) => 'q = "from"
@send external insert: ('db, 'table) => 'builder = "insert"
@send external values: ('builder, 'row) => 'builder = "values"
@send external update: ('db, 'table) => 'builder = "update"
@send external set: ('builder, 'patch) => 'builder = "set"
@send external deleteFrom: ('db, 'table) => 'builder = "delete"

// --- Query chain refinements -------------------------------------------------
@send external where: ('q, 'expr) => 'q = "where"
@send external orderBy: ('q, 'expr) => 'q = "orderBy"
@send external limit: ('q, int) => 'q = "limit"
@send external offset: ('q, int) => 'q = "offset"
@send external groupBy: ('q, 'expr) => 'q = "groupBy"

// --- Awaitable terminals -----------------------------------------------------
@send external allAsync: 'q => promise<array<'row>> = "all"
@send external getAsync: 'q => promise<Nullable.t<'row>> = "get"
@send external returning: 'q => promise<array<'row>> = "returning"

// --- Comparison operators (drizzle-orm) --------------------------------------
@module("drizzle-orm") external eq: ('col, 'val) => 'expr = "eq"
@module("drizzle-orm") external ne: ('col, 'val) => 'expr = "ne"
@module("drizzle-orm") external gt: ('col, 'val) => 'expr = "gt"
@module("drizzle-orm") external gte: ('col, 'val) => 'expr = "gte"
@module("drizzle-orm") external lt: ('col, 'val) => 'expr = "lt"
@module("drizzle-orm") external lte: ('col, 'val) => 'expr = "lte"
@module("drizzle-orm") external inArray: ('col, array<'val>) => 'expr = "inArray"
@module("drizzle-orm") external notInArray: ('col, array<'val>) => 'expr = "notInArray"
@module("drizzle-orm") external like: ('col, string) => 'expr = "like"
@module("drizzle-orm") external ilike: ('col, string) => 'expr = "ilike"
@module("drizzle-orm") external isNull: 'col => 'expr = "isNull"
@module("drizzle-orm") external isNotNull: 'col => 'expr = "isNotNull"

// --- Boolean combinators -----------------------------------------------------
@module("drizzle-orm") @variadic external and: array<'expr> => 'expr = "and"
@module("drizzle-orm") @variadic external or: array<'expr> => 'expr = "or"
@module("drizzle-orm") external not: 'expr => 'expr = "not"

// --- Ordering helpers --------------------------------------------------------
@module("drizzle-orm") external asc: 'col => 'expr = "asc"
@module("drizzle-orm") external desc: 'col => 'expr = "desc"
