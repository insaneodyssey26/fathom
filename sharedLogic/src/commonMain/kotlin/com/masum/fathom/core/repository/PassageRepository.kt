package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Passage
import kotlinx.coroutines.flow.Flow

interface PassageRepository {
    suspend fun getPassage(id: String): FathomResult<Passage, DataError.Local>
    suspend fun getPassagesForBook(bookId: String, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local>
    fun observePassage(id: String): Flow<FathomResult<Passage, DataError.Local>>
}
