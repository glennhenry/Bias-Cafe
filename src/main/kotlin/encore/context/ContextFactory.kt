package encore.context

import encore.subunit.scope.ServerScope
import kotlinx.coroutines.CoroutineScope
import project.context.ServerContext

/**
 * Represent a factory responsible for context objects creation such as
 * [ServerContext].
 */
interface ContextFactory {
    /**
     * Create [ServerContext].
     *
     * @param appScope The application root coroutine scope.
     * @param serverSubunitScope Server subunit scope.
     */
    suspend fun serverContext(
        appScope: CoroutineScope,
        serverSubunitScope: ServerScope
    ): ServerContext
}
