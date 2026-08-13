package projectTest

import encore.subunit.scope.ServerScope
import encore.time.source.MutableTimeSource
import encore.utils.identifier.Ids
import kotlinx.coroutines.test.runTest
import portal.domain.session.SessionStore
import portal.domain.session.SessionStoreModel
import portal.domain.session.WebsiteSessionSubunit
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days

class WebsiteSessionSubunitTest {
    @Test
    fun testVerify() = runTest {
        val time = MutableTimeSource()
        val session = WebsiteSessionSubunit(this, time, sessionStore = object : SessionStore {
            override suspend fun load(): Result<List<SessionStoreModel>> {
                return runCatching { emptyList() }
            }

            override suspend fun put(
                userId: String,
                token: String,
                expiresAt: Long
            ): Result<Unit> {
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

        val token = session.create(Ids.uuid())
        assertNotNull(session.verify(token))
        time.controller.forwardBy(366.days)
        assertNull(session.verify(token))
        session.disband(ServerScope)
    }
}
