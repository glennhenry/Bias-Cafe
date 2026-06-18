import bootstrap.*
import encore.EncoreIdentity
import encore.EncoreIdentity.celebrate
import encore.backstage.BackstageRoutes
import encore.backstage.command.ExampleCommand
import encore.context.ServerContext
import encore.datastore.MongoCollectionName
import encore.route.guard.DefaultSecurity
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.time.source.SystemTimeSource
import encore.venue.Venue
import encore.websocket.handler.WsCommandHandler
import project.ProjectIdentity
import project.Globals
import project.fileRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

fun main() {
    Venue.prepare()

    // override Ktor dev mode with the framework custom config
    System.setProperty("io.ktor.development", Venue.encore.devMode.toString())

    embeddedServer(
        factory = Netty,
        host = Venue.encore.server.host,
        port = Venue.encore.server.port,
        watchPaths = listOf("classes")
    ) { configureApplication() }.start(wait = true)
}

val MongoCollectionName = MongoCollectionName(
    playerAccount = "player_account",
    playerObjects = "player_objects",
    playerServerObjects = "player_server_objects",
    serverObjects = "server_objects"
)

val SystemTimezone: ZoneId = ZoneId.systemDefault()

/**
 * Main configuration and wiring code for the application.
 */
suspend fun Application.configureApplication() {
    // install system time
    TimeCenter.update(source = SystemTimeSource())

    // configure security
    val bannedAddresses = mutableSetOf<String>()
    val security = DefaultSecurity(bannedAddresses, TimeCenter.source)

    // setup the framework
    val (mongoc, db) = installEncore(
        module = SerializersModule { },
        security = security
    )

    // creates a coroutine scope for the app
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val serverSubunitScope = ServerScope

    // create server context
    val serverContext = createServerContext(
        appScope = appScope,
        serverSubunitScope = serverSubunitScope,
        collectionName = MongoCollectionName,
        mongoClient = mongoc,
        mongoDatabase = db
    )

    // create admin account
    if (Venue.encore.adminEnabled) {
        serverContext.subunits.creation.createAdmin(Globals, alwaysRecreate = false)
    }

    // register handlers for WebSocket
    websocketHandlers(serverContext)

    // register commands
    commandHandlers(serverContext)

    // configure routing
    // ephemeral token storage for /backstage entry
    val backstageToken = ConcurrentHashMap<String, Long>()

    // install routes
    routing {
        fileRoutes()
        with(BackstageRoutes(serverContext, backstageToken)) { install() }
    }

    // log startup
    logStartupInformation()

    // starts accepting terminal input
    acceptsTerminalInput(appScope, backstageToken)

    // prints encore banner
    println(EncoreIdentity.banner(ProjectIdentity))
    celebrate(LocalDate.now(SystemTimezone))

    // install shutdown hook
    shutdownHook(appScope, serverSubunitScope, serverContext.subunits)
}

fun websocketHandlers(serverContext: ServerContext) {
    with(serverContext.webSocketManager) {
        registerHandler(WsCommandHandler(serverContext))
    }
}

fun commandHandlers(serverContext: ServerContext) {
    with(serverContext.commandDispatcher) {
        register(ExampleCommand())
    }
}
