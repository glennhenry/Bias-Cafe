package encore.context

import encore.subunit.scope.ServerScope
import kotlinx.coroutines.CoroutineScope
import project.context.ServerContext

/**
 * Fake implementation of [ContextFactory]
 * - [serverContext] creation is provided from constructor, or default with
 *   [ServerContext.createForTest].
 */
class FakeContextFactory : ContextFactory {
    override suspend fun serverContext(
        appScope: CoroutineScope,
        serverSubunitScope: ServerScope
    ): ServerContext {
        return ServerContext.createForTest()
    }
}
