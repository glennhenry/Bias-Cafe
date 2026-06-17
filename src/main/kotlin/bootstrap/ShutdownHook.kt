package bootstrap

import encore.context.ServerSubunits
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.subunit.scope.ServerScope
import encore.subunit.Subunit
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking

/**
 * Install a hook for the application shutdown which will:
 * - Disband every server [Subunit]
 * - Cancels the application coroutine [appScope]
 */
fun shutdownHook(
    appScope: CoroutineScope,
    serverSubunitScope: ServerScope,
    subunits: ServerSubunits,
) {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            try {
                subunits.disband(serverSubunitScope)
                appScope.cancel("Application closed")
                appScope.coroutineContext.job.cancel()
            } catch (_: CancellationException) {
            }
        }
        Fancam.info(Tags.Shutdown) { "Server shutdown complete." }
    })
}
