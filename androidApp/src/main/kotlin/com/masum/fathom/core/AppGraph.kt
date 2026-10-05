package com.masum.fathom.core

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.masum.fathom.core.database.SqlDelightPassageRepository
import com.masum.fathom.core.database.SqlDelightUserDataRepository
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.feed.FeedEngine
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import java.io.FileOutputStream

class AppGraph(context: Context) {
    val feedEngine: FeedEngine
    val userRepository: UserDataRepository
    val passageRepository: PassageRepository

    init {
        installContentDatabase(context)

        val userDriver = AndroidSqliteDriver(FathomUserDatabase.Schema, context, "app_user_data.db")
        val contentDriver = AndroidSqliteDriver(
            schema = object : SqlSchema<QueryResult.Value<Unit>> {
                override val version: Long get() = 1L
                override fun create(driver: SqlDriver): QueryResult.Value<Unit> = QueryResult.Unit
                override fun migrate(
                    driver: SqlDriver,
                    oldVersion: Long,
                    newVersion: Long,
                    vararg callbacks: app.cash.sqldelight.db.AfterVersion
                ): QueryResult.Value<Unit> = QueryResult.Unit
            },
            context = context,
            name = CONTENT_DB
        )

        val userDb = FathomUserDatabase(userDriver)
        val contentDb = FathomContentDatabase(contentDriver)

        feedEngine = FeedEngine(contentDb, userDb)
        userRepository = SqlDelightUserDataRepository(userDb)
        passageRepository = SqlDelightPassageRepository(contentDb)
    }

    private fun installContentDatabase(context: Context) {
        val dbFile = context.getDatabasePath(CONTENT_DB)
        val prefs = context.getSharedPreferences("fathom_content_prefs", Context.MODE_PRIVATE)
        val installedVersion = prefs.getInt("db_asset_version", 0)
        val needsExtraction = !dbFile.exists() || dbFile.length() < 100_000L || installedVersion < CONTENT_DB_VERSION

        if (!needsExtraction) return

        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()
        context.assets.open(CONTENT_DB).use { input ->
            FileOutputStream(dbFile).use { output -> input.copyTo(output) }
        }
        prefs.edit().putInt("db_asset_version", CONTENT_DB_VERSION).apply()
    }

    private companion object {
        const val CONTENT_DB = "starter_content.db"
        const val CONTENT_DB_VERSION = 10
    }
}
