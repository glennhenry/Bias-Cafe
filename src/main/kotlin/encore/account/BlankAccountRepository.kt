package encore.account

import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.account.model.Credentials
import encore.account.model.Profile

/**
 * No-operation implementation of [AccountRepository] used for testing purposes.
 */
class BlankAccountRepository : AccountRepository {
    override suspend fun getAccountByUsername(username: String): Result<UserAccount?> = TODO("NO OPERATION")
    override suspend fun getUserIdByUsername(username: String): Result<UserId?> = TODO("NO OPERATION")
    override suspend fun getProfile(userId: UserId): Result<Profile?> = TODO("NO OPERATION")
    override suspend fun getCredentials(username: String): Result<Credentials?> = TODO("NO OPERATION")
    override suspend fun updateUserAccount(userId: UserId, account: UserAccount): Result<Unit> = TODO("NO OPERATION")
    override suspend fun updateProfile(userId: UserId, profile: Profile): Result<Unit> = TODO("NO OPERATION")
    override suspend fun updateLastActivity(userId: UserId, lastActivity: Long): Result<Unit> = Result.success(Unit)
    override suspend fun usernameExists(username: String): Result<Boolean> = TODO("NO OPERATION")
    override suspend fun emailExists(email: String): Result<Boolean> = TODO("NO OPERATION")
}
