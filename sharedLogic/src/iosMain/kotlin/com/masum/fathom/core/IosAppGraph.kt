package com.masum.fathom.core

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.masum.fathom.core.database.SqlDelightPassageRepository
import com.masum.fathom.core.database.SqlDelightUserDataRepository
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.feed.FeedEngine
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

class IosAppGraph {
    val feedEngine: FeedEngine
    val userRepository: UserDataRepository
    val passageRepository: PassageRepository

    init {
        installContentDatabase()

        val userDriver = NativeSqliteDriver(FathomUserDatabase.Schema, "app_user_data.db")
        val contentDriver = NativeSqliteDriver(
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
            name = CONTENT_DB
        )

        val userDb = FathomUserDatabase(userDriver)
        val contentDb = FathomContentDatabase(contentDriver)

        feedEngine = FeedEngine(contentDb, userDb)
        userRepository = SqlDelightUserDataRepository(userDb)
        passageRepository = SqlDelightPassageRepository(contentDb)
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun installContentDatabase() {
        val fileManager = NSFileManager.defaultManager
        val appSupportDir = fileManager.URLsForDirectory(
            NSApplicationSupportDirectory,
            NSUserDomainMask
        ).firstOrNull() as? NSURL ?: return

        val databasesDir = appSupportDir.URLByAppendingPathComponent("databases") ?: return
        val databasesPath = databasesDir.path ?: return
        if (!fileManager.fileExistsAtPath(databasesPath)) {
            fileManager.createDirectoryAtPath(databasesPath, true, null, null)
        }

        val destDbUrl = databasesDir.URLByAppendingPathComponent(CONTENT_DB) ?: return
        val destPath = destDbUrl.path ?: return

        val userDefaults = NSUserDefaults.standardUserDefaults
        val installedVersion = userDefaults.integerForKey("db_asset_version").toInt()
        val fileExists = fileManager.fileExistsAtPath(destPath)

        if (!fileExists || installedVersion < CONTENT_DB_VERSION) {
            val bundlePath = NSBundle.mainBundle.pathForResource("starter_content", "db")
            if (bundlePath != null && fileManager.fileExistsAtPath(bundlePath)) {
                if (fileExists) {
                    fileManager.removeItemAtPath(destPath, null)
                }
                fileManager.copyItemAtPath(bundlePath, destPath, null)
                userDefaults.setInteger(CONTENT_DB_VERSION.toLong(), "db_asset_version")
            }
        }
    }

    companion object {
        const val CONTENT_DB = "starter_content.db"
        const val CONTENT_DB_VERSION = 10
    }
}
