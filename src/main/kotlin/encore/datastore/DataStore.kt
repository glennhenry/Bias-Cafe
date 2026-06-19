package encore.datastore

import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.datastore.collection.ServerObjects

/**
 * A suspendable persistence component that provides access to user and server data.
 *
 * The data is separated into four core collections:
 * - [UserAccount]: user's account data.
 * - [ServerObjects]: server's operational or game data.
 *
 * Implementation exposes way to retrieve the core collections and user creation.
 *
 * Higher-level operations such as user creation or alteration of certain user
 * or server objects fields should be handled by subunits separately per-domain.
 */
interface DataStore {
    /**
     * Ensures the data store is fully initialized.
     *
     * This suspend function will wait until any asynchronous setup is complete.
     * Call this before performing operations that require the store to be ready.
     */
    suspend fun awaitInit()

    /**
     * Returns whether an account associated with [userId] exists.
     */
    suspend fun userExists(userId: UserId): Boolean

    /**
     * Returns the [UserAccount] for the given [userId].
     */
    suspend fun getUserAccount(userId: UserId): UserAccount?

    /**
     * Returns the [ServerObjects] (global server data).
     */
    suspend fun getServerObjects(): ServerObjects?

    /**
     * Creates a new user with the given account and objects.
     *
     * @return [Result] type denoting success or failure.
     */
    suspend fun create(
        account: UserAccount,
    ): Result<Unit>

    /**
     * Deletes a user associated with the [userId].
     */
    suspend fun delete(userId: UserId): Result<Unit>

    /**
     * Shutdown the data store.
     *
     * This should contains the necessary clean-up code before closing.
     */
    suspend fun shutdown()
}
