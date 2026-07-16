package encore.account

import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.account.model.Credentials
import encore.datastore.collection.Profile

/**
 * Repository for [UserAccount] core collection.
 *
 * Implementation should abstract the data access to user accounts.
 * For instance:
 * - Mongo implementation provides query and update APIs from the
 *   underlying `MongoCollection`.
 * - SQL implementation provides similar APIs to the account table.
 * - In-memory implementation provides APIs using an in-memory storage.
 *
 * Each operation should return a [Result] type to denote the outcome.
 * [Result.failure] should be used when the operation fails due to an
 * internal failure such as DB errors and not business outcome.
 */
interface AccountRepository {
    /**
     * Returns [UserAccount] associated with the given [userId], if it exists.
     *
     * Returns [Result.success] with:
     * - the [UserAccount] if found
     * - `null` if no account exists for the given [userId]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getAccountByUserId(userId: String): Result<UserAccount?>

    /**
     * Returns [UserAccount] associated with the given [username], if it exists.
     *
     * Returns [Result.success] with:
     * - the [UserAccount] if found
     * - `null` if no account exists for the given [username]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getAccountByUsername(username: String): Result<UserAccount?>

    /**
     * Returns [UserId] associated with the given [username], if it exists.
     *
     * Returns [Result.success] with:
     * - the [UserId] if found
     * - `null` if no account exists for the given [username]
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun getUserIdByUsername(username: String): Result<UserId?>


    /**
     * Returns the [Credentials] of the provided [username], if the account exists.
     */
    suspend fun getCredentials(username: String): Result<Credentials?>

    /**
     * Update [UserAccount] of [userId] with the new [account].
     * @return [Result] type denoting success or failure.
     */
    suspend fun updateUserAccount(userId: UserId, account: UserAccount): Result<Unit>

    /**
     * Update [UserAccount.lastActiveAt] of [userId] with [lastActivity].
     * @return [Result] type denoting success or failure.
     */
    suspend fun updateLastActivity(userId: UserId, lastActivity: Long): Result<Unit>

    /**
     * Returns whether the provided [username] already exists.
     *
     * Returns [Result.success] with:
     * - [Result.value]` = true` if username exists
     * - [Result.value]` = false` if username does not exist
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun usernameExists(username: String): Result<Boolean>

    /**
     * Returns whether the provided [email] already exists.
     *
     * Returns [Result.success] with:
     * - [Result.value]` = true` if email exists
     * - [Result.value]` = false` if email does not exist
     *
     * Returns [Result.failure] if an error occurs while retrieving the data.
     */
    suspend fun emailExists(email: String): Result<Boolean>
}
