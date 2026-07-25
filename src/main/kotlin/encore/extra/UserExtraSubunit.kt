package encore.extra

import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.toOutcome
import encore.utils.types.toReport
import project.mongo.collection.UserId

/**
 * Server subunit for [UserExtraRepository].
 */
class UserExtraSubunit(
    private val extraRepository: UserExtraRepository
) : Subunit<ServerScope> {
    suspend fun getExtra(userId: UserId, key: String): Outcome<String?> {
        return extraRepository.getExtra(userId, key)
            .onFailure {
                Fancam.error(it, Tags.Extra) {
                    "getExtra failed: repository scandal for '$userId' on key=$key"
                }
            }
            .toOutcome { ext -> return Outcome.Ok(ext) }
    }

    suspend fun getAllExtra(userId: UserId, key: String): Outcome<Map<String, String>?> {
        return extraRepository.getAllExtra(userId, key)
            .onFailure {
                Fancam.error(it, Tags.Extra) {
                    "getAllExtra failed: repository scandal for '$userId' on key=$key"
                }
            }
            .toOutcome { ext -> return Outcome.Ok(ext) }
    }

    suspend fun updateExtra(userId: UserId, key: String, value: String): Report {
        return extraRepository.updateExtra(userId, key, value)
            .onFailure {
                Fancam.error(it, Tags.Extra) {
                    "updateExtra failed: repository scandal for '$userId' on key=$key to value=$value"
                }
            }
            .toReport()
    }

    suspend fun deleteExtra(userId: UserId, key: String): Report {
        return extraRepository.deleteExtra(userId, key)
            .onFailure {
                Fancam.error(it, Tags.Extra) {
                    "deleteExtra failed: repository scandal for '$userId' on key=$key"
                }
            }
            .toReport()
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    override suspend fun disband(scope: ServerScope): Result<Unit> {
        return runCatching { }
    }

    companion object {
        fun createForTest(extraRepository: UserExtraRepository): UserExtraSubunit {
            return UserExtraSubunit(extraRepository)
        }
    }
}
