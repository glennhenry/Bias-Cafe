package encore.context

import encore.account.AccountRepository
import encore.account.AccountSubunit
import encore.account.BlankAccountRepository
import encore.account.UserCreationSubunit
import encore.presence.UserPresenceSubunit
import encore.acts.ActIdStore
import encore.acts.StageActDirector
import encore.auth.AuthSubunit
import encore.backstage.command.CommandDispatcher
import encore.datastore.BlankDataStore
import encore.datastore.DataStore
import encore.fancam.Fancam
import encore.session.SessionSubunit
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.time.source.SystemTimeSource
import encore.time.source.TimeSource
import encore.utils.support.className
import encore.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import project.domain.profile.BlankProfileRepository
import project.domain.profile.ProfileRepository
import project.domain.profile.ProfileSubunit
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Represents the **global server-side context**.
 *
 * `ServerContext` includes various server-side components needed on the server.
 * It acts as a dependency container which is distributed across the server code.
 *
 * @property dataStore [DataStore] instance of the server.
 * @property commandDispatcher Tracks and executes server commands.
 * @property stageActDirector Provide API to start and stop stage acts.
 * @property webSocketManager Manages client websocket connections.
 * @property subunits Container for server subunit instances.
 */
data class ServerContext(
    val dataStore: DataStore,
    val commandDispatcher: CommandDispatcher,
    val stageActDirector: StageActDirector,
    val webSocketManager: WebSocketManager,
    val subunits: ServerSubunits
) {
    companion object {
        /**
         * Creates a test instance of [ServerContext].
         *
         * @param parentScope `CoroutineScope` for [SessionSubunit].
         * @param timeSource [TimeSource] for [StageActDirector].
         * @param dataStore Also used to build [UserCreationSubunit].
         * @param accountRepository Used to build [AccountSubunit].
         * @param profileRepository Used to build [ProfileSubunit].
         */
        fun createForTest(
            parentScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
            timeSource: TimeSource = SystemTimeSource(),
            dataStore: DataStore = BlankDataStore(),
            accountRepository: AccountRepository = BlankAccountRepository(),
            profileRepository: ProfileRepository = BlankProfileRepository(),
        ): ServerContext {
            val account = AccountSubunit(accountRepository)
            val profile = ProfileSubunit(profileRepository)
            val session = SessionSubunit.createForTest(parentScope)
            val creation = UserCreationSubunit.createForTest(dataStore)

            return ServerContext(
                dataStore = dataStore,
                commandDispatcher = CommandDispatcher(),
                stageActDirector = StageActDirector(timeSource, ActIdStore),
                webSocketManager = WebSocketManager(),
                subunits = ServerSubunits(
                    account = account,
                    profile = profile,
                    auth = AuthSubunit(account, creation, session),
                    creation = creation,
                    presence = UserPresenceSubunit(),
                    session = session
                )
            )
        }
    }
}

/**
 * Container for all server-scoped [Subunit] instances.
 *
 * Server subunits encapsulate domain logic that operates at the server level.
 * They may manage shared state or provide global domain functionality,
 * with or without persistent data.
 *
 * Server subunits are typically bound to [ServerScope].
 *
 * Examples:
 * - An infra-related component providing session creation and verification.
 * - A leaderboard representing global state is not owned by any single user.
 *   A `LeaderboardSubunit` may expose operations to query or update rankings.
 * - A matchmaking system may not persist data, but can maintain in-memory
 *   state and provide matchmaking-specific functionality.
 *
 * @property account Provides API related to accounts.
 * @property profile Provides API related to profiles.
 * @property auth Provides authentication functions.
 * @property creation Provides user creation mechanism.
 * @property presence Tracks user's presence.
 * @property session Manages session of users.
 */
data class ServerSubunits(
    val account: AccountSubunit,
    val profile: ProfileSubunit,
    val auth: AuthSubunit,
    val creation: UserCreationSubunit,
    val presence: UserPresenceSubunit,
    val session: SessionSubunit,
) {
    /**
     * Return all server subunit instances.
     */
    fun all(): Set<Subunit<ServerScope>> {
        return setOf(account, profile, auth, creation, presence, session)
    }

    /**
     * Debut every server subunit instances with [scope].
     */
    suspend fun debut(scope: ServerScope) {
        all().forEach { subunit ->
            val result = subunit.debut(scope)
            if (result.isFailure) {
                Fancam.error(result.exceptionOrNull()) { "Result.failure on ServerSubunit debut '${subunit.className()}'" }
            }
        }
    }

    /**
     * Disband every server subunit instances with [scope].
     */
    suspend fun disband(scope: ServerScope) {
        all().forEach { subunit ->
            val result = subunit.disband(scope)
            if (result.isFailure) {
                Fancam.error(result.exceptionOrNull()) { "Result.failure on ServerSubunit disband '${subunit.className()}'" }
            }
        }
    }
}
