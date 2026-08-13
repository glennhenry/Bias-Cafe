package encore.extra

import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId

/**
 * Repository handling [UserAccount.extra] concern.
 */
interface UserExtraRepository {
    /**
     * Retrieve the extra data of [key] owned by [userId].
     * @return [Result.failure] for any repository error,
     *         otherwise [Result.success] with the value.
     */
    suspend fun getExtra(userId: UserId, key: String): Result<String?>

    /**
     * Retrieve all extra data owned by [userId].
     * @return [Result.failure] for any repository error,
     *         otherwise [Result.success] with the value.
     */
    suspend fun getAllExtra(userId: UserId, key: String): Result<Map<String, String>?>

    /**
     * Update the extra data of [key] owned by [userId] by [value].
     * @return [Result.failure] for any repository error, otherwise [Result.success].
     */
    suspend fun updateExtra(userId: UserId, key: String, value: String): Result<Unit>

    /**
     * Delete the extra data of [key] owned by [userId].
     * @return [Result.failure] for any repository error, otherwise [Result.success].
     */
    suspend fun deleteExtra(userId: UserId, key: String): Result<Unit>
}
