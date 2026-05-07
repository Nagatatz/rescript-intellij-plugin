package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DatabaseTest {
    @Test
    fun `variantKey lowercases the enum name so it can index into resource paths`() {
        assertEquals("libsql", Database.LIBSQL.variantKey())
        assertEquals("postgres", Database.POSTGRES.variantKey())
        assertEquals("mysql", Database.MYSQL.variantKey())
    }

    @Test
    fun `toString returns the user-visible label not the enum constant name`() {
        assertEquals("libSQL / SQLite", Database.LIBSQL.toString())
        assertEquals("PostgreSQL", Database.POSTGRES.toString())
        assertEquals("MySQL", Database.MYSQL.toString())
    }
}
