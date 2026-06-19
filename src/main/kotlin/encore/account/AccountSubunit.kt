package encore.account

import encore.account.model.Credentials
import encore.auth.AuthSubunit
import encore.datastore.collection.UserAccount
import encore.datastore.collection.UserId
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.toOutcome
import encore.utils.types.toReport

/**
 * Server subunits that handles [UserAccount] concerns from [AccountRepository].
 *
 * This subunit focuses on abstracting low-level API of `AccountRepository`.
 * Every operations here return a [Report] or [Outcome] type. It doesn't interpret
 * or handle the result from repository. Instead, callers (e.g., [AuthSubunit] should be
 * responsible for it.
 *
 * @property accountRepository [AccountRepository] implementation.
 */
class AccountSubunit(private val accountRepository: AccountRepository) : Subunit<ServerScope> {
    /**
     * Returns an [Outcome] containing [UserAccount] associated with [username].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with `null` if account does not exist.
     * - [Outcome.Ok] with the account otherwise.
     */
    suspend fun getAccountByUsername(username: String): Outcome<UserAccount?> {
        return accountRepository.getAccountByUsername(username)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "getAccountByUsername failed: repository scandal for '$username'"
                }
            }
            .toOutcome { account -> return Outcome.Ok(account) }
    }

    /**
     * Returns an [Outcome] containing [UserId] associated with [username].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with `null` if account does not exist.
     * - [Outcome.Ok] with the `userId` otherwise.
     */
    suspend fun getUserIdByUsername(username: String): Outcome<UserId?> {
        return accountRepository.getUserIdByUsername(username)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "getUserIdByUsername failed: repository scandal for '$username'"
                }
            }
            .toOutcome { userId -> return Outcome.Ok(userId) }
    }

    /**
     * Returns an [Outcome] containing the credentials of [username].
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the credentials result.
     */
    suspend fun getCredentials(username: String): Outcome<Credentials?> {
        return accountRepository.getCredentials(username)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "getCredentials failed: repository scandal for '$username'"
                }
                return Outcome.Fail
            }
            .toOutcome { credentials ->
                return Outcome.Ok(credentials)
            }
    }

    /**
     * Update [UserAccount] of [userId].
     * @return [Report] type denoting success or failure.
     */
    suspend fun updateUserAccount(userId: UserId, account: UserAccount): Report {
        return accountRepository.updateUserAccount(userId, account)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "updateUserAccount failed: repository scandal for '$userId' on update with $account"
                }
                return Report.Fail
            }
            .toReport()
    }

    /**
     * Update the last activity of [userId].
     * @return [Report] type denoting success or failure.
     */
    suspend fun updateLastActivity(userId: UserId, lastActivity: Long): Report {
        return accountRepository.updateLastActivity(userId, lastActivity)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "updateLastActivity failed: repository scandal for '$userId', lastActivity=$lastActivity"
                }
            }
            .toReport()
    }

    /**
     * Returns an [Outcome] describing whether [username] exists or not.
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the existence result.
     */
    suspend fun usernameExists(username: String): Outcome<Boolean> {
        return accountRepository.usernameExists(username)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "Username check failed: repository scandal for '$username'"
                }
            }
            .toOutcome { exists -> return Outcome.Ok(exists) }
    }

    /**
     * Returns an [Outcome] describing whether [email] exists or not.
     * - [Outcome.Fail] when there is internal repository error.
     * - [Outcome.Ok] with the existence result.
     */
    suspend fun emailExists(email: String): Outcome<Boolean> {
        return accountRepository.emailExists(email)
            .onFailure {
                Fancam.error(it, Tags.Account) {
                    "Email check failed: repository scandal for '$email'"
                }
            }
            .toOutcome { exists -> return Outcome.Ok(exists) }
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> = Result.success(Unit)
    override suspend fun disband(scope: ServerScope): Result<Unit> = Result.success(Unit)

    companion object {
        /**
         * Creates a test instance of [AccountSubunit].
         *
         * @param accountRepository use [BlankAccountRepository] when not under test.
         */
        fun createForTest(
            accountRepository: AccountRepository = BlankAccountRepository()
        ): AccountSubunit {
            return AccountSubunit(accountRepository)
        }
    }
}
