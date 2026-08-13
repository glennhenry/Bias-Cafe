package portal.routes

import bootstrap.errorHtml
import encore.route.RouteHandler
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

/**
 * Serve file-related endpoints.
 *
 * This mostly serving static files:
 * - Game and website assets in the `assets` folder.
 * - Docs website on production in the `docs_build` folder.
 *
 * Since this is simple, it doesn't use the [RouteHandler]
 */
fun Route.fileRoutes() {
    // serve site assets
    staticFiles("site", File("assets/site"))
    staticFiles("icons", File("assets/icons"))
    staticFiles("images", File("assets/images"))
    staticFiles("avatars", File("assets/avatars"))
    get("/favicon.ico") { call.respondFile(File("assets/site/favicon.ico")) }

    val docsDir = File("docs_build")
    if (File(docsDir, "index.html").exists()) {
        staticFiles("docs", docsDir)
    } else {
        get("/docs/{...}") {
            call.respondText(
                text = errorHtml(404, DocsNotFoundMessage),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.NotFound
            )
        }
    }
}

const val DocsNotFoundMessage = """
Docs website is not available.<br><br>

If you are in <strong>development mode</strong>, 
please start with a separate vite server, 
then access <a href='http://localhost:4321/docs/' target='_blank'>
http://localhost:4321/docs/</a><br><br>

If you are in <strong>production mode</strong>,
you need to build the documentation website to access it.<br>
"""
