package com.masum.fathom.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase

actual fun createTestUserDriver(): SqlDriver {
    return NativeSqliteDriver(FathomUserDatabase.Schema, "test_user_db")
}

actual fun createTestContentDriver(): SqlDriver {
    return NativeSqliteDriver(FathomContentDatabase.Schema, "test_content_db")
}
