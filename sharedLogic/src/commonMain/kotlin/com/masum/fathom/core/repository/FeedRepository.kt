package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.user.FeedConfig

interface FeedRepository {
    suspend fun getFeedBatch(config: FeedConfig, batchSize: Int): FathomResult<List<Passage>, DataError.Local>
}
