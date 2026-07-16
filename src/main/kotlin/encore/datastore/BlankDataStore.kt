package encore.datastore

import encore.datastore.collection.Profile
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.datastore.collection.ServerObjects

/**
 * No-operation implementation for [DataStore] used for testing purposes.
 */
class BlankDataStore : DataStore {
    override suspend fun awaitInit() = Unit
    override suspend fun userExists(userId: UserId): Boolean = TODO("NO OPERATION")
    override suspend fun getUserAccount(userId: UserId): UserAccount = TODO("NO OPERATION")
    override suspend fun getServerObjects(): ServerObjects = TODO("NO OPERATION")
    override suspend fun create(account: UserAccount): Result<Unit> = TODO("NO OPERATION")
    override suspend fun delete(userId: UserId): Result<Unit> = TODO("NO OPERATION")
    override suspend fun shutdown() = TODO("NO OPERATION")
}
