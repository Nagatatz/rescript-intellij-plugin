// Drizzle schema + Zod schemas, kept side-by-side for a single source of truth.
@module("drizzle-orm/sqlite-core")
external sqliteTable: (string, 'columns) => 'table = "sqliteTable"

@module("drizzle-orm/sqlite-core")
external intCol: (string, 'opts) => 'col = "integer"
@module("drizzle-orm/sqlite-core")
external textCol: (string, 'opts) => 'col = "text"

// Drizzle SQLite `users` table.
let users = sqliteTable("users", {
  "id": intCol("id", {"primaryKey": true, "autoIncrement": true}),
  "name": textCol("name", {"notNull": true}),
  "email": textCol("email", {"notNull": true}),
})

// Zod schemas reused for runtime validation and OpenAPI spec generation.
type zSchema
@module("zod") external z: {
  "object": 'shape => zSchema,
  "string": unit => zSchema,
  "number": unit => zSchema,
} = "z"
@send external openapi: (zSchema, 'meta) => zSchema = "openapi"

let createUserSchema = z["object"]({
  "name": z["string"](),
  "email": z["string"](),
})->openapi({"example": {"name": "Ada", "email": "ada@example.com"}})

let userSchema = z["object"]({
  "id": z["number"](),
  "name": z["string"](),
  "email": z["string"](),
})