package com.masum.fathom.core.database

import app.cash.sqldelight.db.SqlDriver

expect fun createTestUserDriver(): SqlDriver
expect fun createTestContentDriver(): SqlDriver
