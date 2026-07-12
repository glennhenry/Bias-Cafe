package project.domain.session

import encore.fancam.Fancam
import encore.session.SessionSubunit
import encore.subunit.Subunit
import encore.subunit.scope.ServerScope
import encore.time.source.MutableTimeSource
import encore.time.source.TimeSource
import encore.utils.identifier.Ids
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.days

/**
 * A server subunit similar to [SessionSubunit] but adapted specifically
 * for website sessions.
 *
 * This component work with a suspendable operations from [SessionStore],
 * meaning each usage will be suspendable.
 *
 * @property coroutineScope Coroutine scope to run background cleanup task.
 * @property sessionStore Implementation of [SessionStore].
 */
class WebsiteSessionSubunit(
    private val coroutineScope: CoroutineScope,
    private val timeSource: TimeSource,
    private val sessionStore: SessionStore
) : Subunit<ServerScope> {
    private val sessions = ConcurrentHashMap<String, SessionMemoryModel>()

    private val cleanUpInterval = 1.days
    private val cleanupJob: Job = coroutineScope.launch {
        while (isActive) {
            delay(cleanUpInterval)
            cleanupExpiredSessions()
        }
    }

    private val sessionDuration = 365.days
    private val refreshThreshold = 335.days // 30 days passed

    /**
     * To create a new session which will be valid for 365 days.
     * @return The newly created session identifier.
     */
    suspend fun create(): String {
        val now = timeSource.now()
        val expiresAt = now + sessionDuration.inWholeMilliseconds
        val id = Ids.uuid()

        sessions[id] = SessionMemoryModel(id, expiresAt)
        sessionStore.put(id, expiresAt)
            .onFailure {
                Fancam.error(it, "web_session") { "Failed to create website session" }
            }

        return id
    }

    /**
     * Verify the validity of session associated with the [token].
     *
     * This checks whether the token is valid:
     * - the token actually exists
     * - the token doesn't expire yet
     *
     * This will also refresh the session back to 365 days
     * if at least 30 days has passed.
     *
     * @return `true` if session is valid, `false` otherwise.
     */
    suspend fun verify(token: String): Boolean {
        val session = sessions[token] ?: return false
        val now = timeSource.now()

        // expired
        if (now >= session.expiresAt) {
            delete(token)
            return false
        }

        // optionally, refresh the token
        if ((session.expiresAt - now).days <= refreshThreshold) {
            session.expiresAt = sessionDuration.inWholeMilliseconds
            sessionStore.update(token, sessionDuration.inWholeMilliseconds)
                .onFailure {
                    Fancam.error(it, "web_session") { "Failed to update website session" }
                }
        }

        return true
    }

    /**
     * Used to delete a session by it's [token], usually when user logs out.
     */
    suspend fun delete(token: String) {
        sessions.remove(token)
        sessionStore.delete(token)
            .onFailure {
                Fancam.error(it, "web_session") { "Failed to delete website session" }
            }
    }

    private suspend fun cleanupExpiredSessions() {
        val now = timeSource.now()

        // clear in memory
        sessions.entries.removeIf { (_, session) ->
            session.expiresAt <= now
        }

        // clear in store
        sessionStore.batchDeleteExpiredSessions(now)
            .onFailure {
                Fancam.error(it, "web_session") {
                    "Scandal on website session batchDeleteExpiredSessions"
                }
            }
    }

    override suspend fun debut(scope: ServerScope): Result<Unit> {
        return runCatching {
            val result = sessionStore.load().getOrThrow()
            result.forEach { model ->
                sessions[model.token] = SessionMemoryModel(model.token, model.expiresAt)
            }

            // clean session on startup
            cleanupExpiredSessions()
        }
    }

    override suspend fun disband(scope: ServerScope): Result<Unit> {
        return runCatching {
            cleanupJob.cancelAndJoin()
            return Result.success(Unit)
        }
    }

    companion object {
        /**
         * Creates a test instance of [WebsiteSessionSubunit].
         *
         * @param coroutineScope coroutine scope used for cleanup job.
         * @param timeSource time model used to control session timing (e.g., [MutableTimeSource]).
         * @param sessionStore [SessionStore] implementation (default to [BlankSessionStore] for no usage).
         */
        fun createForTest(
            coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
            timeSource: TimeSource = MutableTimeSource(),
            sessionStore: SessionStore = BlankSessionStore()
        ): WebsiteSessionSubunit {
            return WebsiteSessionSubunit(coroutineScope, timeSource, sessionStore)
        }
    }
}

data class SessionMemoryModel(
    val token: String,
    var expiresAt: Long
)
