package encore.datastore

import project.mongo.collection.UserAccount
import project.mongo.collection.UserId

/**
 * No-operation implementation for [DataStore] used for testing purposes.
 */
class BlankDataStore : DataStore {
    override suspend fun awaitInit() = Unit
    override suspend fun accountExists(userId: UserId): Boolean = TODO("NO OPERATION")
    override suspend fun insert(account: UserAccount): Result<Unit> = TODO("NO OPERATION")
    override suspend fun delete(userId: UserId): Result<Unit> = TODO("NO OPERATION")
    override suspend fun shutdown() = TODO("NO OPERATION")
}
