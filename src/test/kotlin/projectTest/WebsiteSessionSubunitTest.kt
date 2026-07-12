package projectTest

import encore.subunit.scope.ServerScope
import encore.time.source.MutableTimeSource
import kotlinx.coroutines.test.runTest
import project.domain.session.SessionStore
import project.domain.session.SessionStoreModel
import project.domain.session.WebsiteSessionSubunit
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class WebsiteSessionSubunitTest {
    @Test
    fun testVerify() = runTest {
        val time = MutableTimeSource()
        val session = WebsiteSessionSubunit(this, time, sessionStore = object : SessionStore {
            override suspend fun load(): Result<List<SessionStoreModel>> {
                return runCatching { emptyList() }
            }

            override suspend fun put(token: String, expiresAt: Long): Result<Unit> {
                return runCatching { }
            }

            override suspend fun update(token: String, expiresAt: Long): Result<Unit> {
                return runCatching { }
            }

            override suspend fun delete(token: String): Result<Unit> {
                return runCatching { }
            }

            override suspend fun batchDeleteExpiredSessions(currentTime: Long): Result<Unit> {
                return runCatching { }
            }
        })

        val token = session.create()
        assertTrue(session.verify(token))
        time.controller.forwardBy(366.days)
        assertFalse(session.verify(token))
        session.disband(ServerScope)
    }
}
