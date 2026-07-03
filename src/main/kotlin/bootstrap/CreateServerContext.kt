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
import project.domain.cafe.collection.CollectionSubunit
import project.domain.cafe.collection.MongoCollectionRepository
import project.domain.cafe.topic.MongoTopicRepository
import project.domain.cafe.topic.TopicSubunit
import project.domain.profile.MongoProfileRepository
import project.domain.profile.ProfileSubunit

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
    ).also { it.awaitInit() }
    val accountRepository = MongoAccountRepository(
        accountCollection = mongoDatabase.getCollection(MongoCollectionName.userAccount)
    )

    val stageActDirector = StageActDirector(
        timeSource = TimeCenter.source,
        actStore = ActIdStore
    )
    val commandDispatcher = CommandDispatcher()
    val webSocketManager = WebSocketManager()

    // setup ServerSubunits
    val accountSubunit = AccountSubunit(accountRepository)
    val userPresenceSubunit = UserPresenceSubunit()
    val sessionSubunit = SessionSubunit(appScope, TimeCenter.source)
    val userCreationSubunit = UserCreationSubunit(dataStore)
    val authSubunit = AuthSubunit(accountSubunit, userCreationSubunit, sessionSubunit)

    val profileRepository = MongoProfileRepository(
        profileCollection = mongoDatabase.getCollection(MongoCollectionName.profile)
    )
    val topicRepository = MongoTopicRepository(
        topicCollection = mongoDatabase.getCollection(MongoCollectionName.topic)
    ).also { it.awaitInit() }
    val collectionRepository = MongoCollectionRepository(
        spaceCollection = mongoDatabase.getCollection(MongoCollectionName.spaces),
        sectionCollection = mongoDatabase.getCollection(MongoCollectionName.sections)
    )

    val profileSubunit = ProfileSubunit(profileRepository)
    val topicSubunit = TopicSubunit(topicRepository)
    val collectionSubunit = CollectionSubunit(collectionRepository)

    val subunits = ServerSubunits(
        account = accountSubunit,
        presence = userPresenceSubunit,
        auth = authSubunit,
        session = sessionSubunit,
        creation = userCreationSubunit,

        profile = profileSubunit,
        topic = topicSubunit,
        collection = collectionSubunit
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
