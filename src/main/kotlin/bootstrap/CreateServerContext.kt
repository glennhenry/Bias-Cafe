package bootstrap

import MongoCollectionName
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.account.AccountSubunit
import encore.account.MongoAccountRepository
import encore.account.UserCreationSubunit
import encore.acts.ActIdStore
import encore.acts.StageActDirector
import encore.auth.AuthSubunit
import encore.backstage.command.CommandDispatcher
import encore.context.ServerContext
import encore.context.ServerSubunits
import encore.datastore.MongoCollectionName
import encore.datastore.MongoDataStore
import encore.presence.UserPresenceSubunit
import encore.session.SessionSubunit
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.venue.Venue
import encore.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import project.domain.MongoProfileRepository
import project.domain.ProfileSubunit

/**
 * Create and return a [ServerContext] instance.
 */
suspend fun createServerContext(
    appScope: CoroutineScope,
    serverSubunitScope: ServerScope,
    collectionName: MongoCollectionName,
    mongoClient: MongoClient,
    mongoDatabase: MongoDatabase
): ServerContext {
    // setup ServerContext
    val dataStore = MongoDataStore(
        db = mongoClient.getDatabase(Venue.encore.database.dbName),
        collectionName = MongoCollectionName
    )
    val accountRepository = MongoAccountRepository(
        accountCollection = mongoDatabase.getCollection(MongoCollectionName.userAccount)
    )
    val profileRepository = MongoProfileRepository(
        profileCollection = mongoDatabase.getCollection(MongoCollectionName.profile)
    )
    val stageActDirector = StageActDirector(
        timeSource = TimeCenter.source,
        actStore = ActIdStore
    )
    val commandDispatcher = CommandDispatcher()
    val webSocketManager = WebSocketManager()

    // setup ServerSubunits
    val accountSubunit = AccountSubunit(accountRepository)
    val profileSubunit = ProfileSubunit(profileRepository)
    val userPresenceSubunit = UserPresenceSubunit()
    val sessionSubunit = SessionSubunit(appScope, TimeCenter.source)
    val userCreationSubunit = UserCreationSubunit(dataStore)
    val authSubunit = AuthSubunit(accountSubunit, userCreationSubunit, sessionSubunit)

    val subunits = ServerSubunits(
        account = accountSubunit,
        profile = profileSubunit,
        presence = userPresenceSubunit,
        auth = authSubunit,
        session = sessionSubunit,
        creation = userCreationSubunit
    )

    // debut all subunits
    subunits.debut(ServerScope)

    val serverContext = ServerContext(
        dataStore = dataStore,
        stageActDirector = stageActDirector,
        commandDispatcher = commandDispatcher,
        webSocketManager = webSocketManager,
        subunits = subunits
    )

    return serverContext
}
