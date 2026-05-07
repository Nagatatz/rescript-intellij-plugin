// Drizzle MySQL schema. `db:generate` reads this file to emit migration SQL.
// HTTP input validation lives in `Validation.res` (zod or sury variant) so this
// file stays focused on persistence.
@module("drizzle-orm/mysql-core")
external mysqlTable: (string, 'columns) => 'table = "mysqlTable"

@module("drizzle-orm/mysql-core") external int: string => 'col = "int"
@module("drizzle-orm/mysql-core") external varchar: (string, 'opts) => 'col = "varchar"
@module("drizzle-orm/mysql-core") external text: string => 'col = "text"

@send external primaryKey: 'col => 'col = "primaryKey"
@send external autoincrement: 'col => 'col = "autoincrement"
@send external notNull: 'col => 'col = "notNull"

let posts = mysqlTable("posts", {
  "id": int("id")->primaryKey->autoincrement,
  "title": varchar("title", {"length": 255})->notNull,
  "body": text("body")->notNull,
})
