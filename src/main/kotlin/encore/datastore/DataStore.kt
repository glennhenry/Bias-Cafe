package encore.datastore

import project.mongo.collection.UserAccount
import project.mongo.collection.UserId

/**
 * Persistence component that owns access to the core database collections
 * and responsible for insertion and deletion.
 *
 * Implementation defines insert and deletion to/from the underlying store.
 * It shouldn't contain retrieval operation or alteration on certain fields.
 * This should be done by separate repository per-domain.
 */
interface DataStore {
    /**
     * Ensures the data store is fully initialized.
     *
     * This suspend function will wait until any asynchronous setup is complete.
     */
    suspend fun awaitInit()

    /**
     * Returns whether an account associated with [userId] exists.
     */
    suspend fun accountExists(userId: UserId): Boolean

    /**
     * Insert these documents for a new user creation.
     * @return [Result] type denoting success or failure.
     */
    suspend fun insert(account: UserAccount): Result<Unit>

    /**
     * Delete documents owned by the user identified by [userId].
     */
    suspend fun delete(userId: UserId): Result<Unit>

    /**
     * Shutdown the data store.
     *
     * This should contains the necessary clean-up code before closing.
     */
    suspend fun shutdown()
}
