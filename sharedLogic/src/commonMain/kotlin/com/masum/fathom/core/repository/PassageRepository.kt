package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import kotlinx.coroutines.flow.Flow

interface PassageRepository {
    suspend fun getPassage(id: String): FathomResult<Passage, DataError.Local>
    suspend fun getPassagesForBook(bookId: String, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local>
    suspend fun getAllBooks(): FathomResult<List<Book>, DataError.Local>
    suspend fun getBooksForTradition(traditionId: TraditionId): FathomResult<List<Book>, DataError.Local>
    suspend fun searchPassages(query: String, traditionId: TraditionId? = null, offset: Int = 0, limit: Int = 50): FathomResult<List<Passage>, DataError.Local>
    suspend fun getChaptersForBook(bookId: String): FathomResult<List<Int>, DataError.Local>
    suspend fun getPassagesForBookChapter(bookId: String, chapter: Int): FathomResult<List<Passage>, DataError.Local>
    fun observePassage(id: String): Flow<FathomResult<Passage, DataError.Local>>
}

