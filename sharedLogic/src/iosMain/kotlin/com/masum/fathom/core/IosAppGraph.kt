package com.masum.fathom.core

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.SqlDelightPassageRepository
import com.masum.fathom.core.database.SqlDelightUserDataRepository
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.feed.FeedEngine
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
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

    suspend fun getBookmarkedPassages(): List<Passage> {
        val bookmarks = userRepository.observeBookmarks().first()
        val list = mutableListOf<Passage>()
        for (b in bookmarks) {
            val res = passageRepository.getPassage(b.passageId)
            if (res is FathomResult.Success) {
                list.add(res.data)
            }
        }
        return list
    }

    suspend fun saveBookmark(passageId: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val bookmark = Bookmark(
            id = "bm_$passageId",
            passageId = passageId,
            createdAtEpochMs = now,
            tags = emptyList()
        )
        userRepository.saveBookmark(bookmark)
    }

    suspend fun removeBookmark(passageId: String) {
        userRepository.removeBookmark(passageId)
    }

    suspend fun isBookmarked(passageId: String): Boolean {
        val bookmarks = userRepository.observeBookmarks().first()
        return bookmarks.any { it.passageId == passageId }
    }

    suspend fun saveNote(passageId: String, content: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val note = UserNote(
            id = "note_${passageId}_$now",
            passageId = passageId,
            content = content,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        userRepository.upsertNote(note)
    }

    suspend fun getNotesForPassage(passageId: String): List<UserNote> {
        return userRepository.observeNotesForPassage(passageId).first()
    }

    suspend fun getAllNotes(): List<UserNote> {
        return userRepository.observeAllNotes().first()
    }

    suspend fun recordReading(passageId: String, bookId: String) {
        userRepository.recordReading(passageId, bookId)
    }

    suspend fun getReadingStreakDays(): Int {
        val history = userRepository.observeReadingHistory().first()
        if (history.isEmpty()) return 0
        return history.map { it.lastSeenAtEpochMs / (1000 * 60 * 60 * 24) }.distinct().size
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
