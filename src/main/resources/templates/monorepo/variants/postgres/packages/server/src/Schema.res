// Drizzle PostgreSQL schema. Shared type lives in @<project>/shared/Types.res;
// the table definition stays here because it is server-only.
@module("drizzle-orm/pg-core")
external pgTable: (string, 'columns) => 'table = "pgTable"

@module("drizzle-orm/pg-core") external serial: string => 'col = "serial"
@module("drizzle-orm/pg-core") external text: string => 'col = "text"

@send external primaryKey: 'col => 'col = "primaryKey"
@send external notNull: 'col => 'col = "notNull"

let users = pgTable("users", {
  "id": serial("id")->primaryKey,
  "name": text("name")->notNull,
  "email": text("email")->notNull,
})
