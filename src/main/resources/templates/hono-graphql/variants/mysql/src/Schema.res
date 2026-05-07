// Drizzle MySQL schema for the GraphQL `users` resolvers. `db:generate` reads
// this file to emit migration SQL.
@module("drizzle-orm/mysql-core")
external mysqlTable: (string, 'columns) => 'table = "mysqlTable"

@module("drizzle-orm/mysql-core") external int: string => 'col = "int"
@module("drizzle-orm/mysql-core") external varchar: (string, 'opts) => 'col = "varchar"

@send external primaryKey: 'col => 'col = "primaryKey"
@send external autoincrement: 'col => 'col = "autoincrement"
@send external notNull: 'col => 'col = "notNull"

let users = mysqlTable("users", {
  "id": int("id")->primaryKey->autoincrement,
  "name": varchar("name", {"length": 255})->notNull,
  "email": varchar("email", {"length": 255})->notNull,
})
