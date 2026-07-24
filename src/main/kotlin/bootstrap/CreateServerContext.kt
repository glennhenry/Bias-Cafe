package bootstrap

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
import encore.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import project.domain.cafe.collection.CollectionSubunit
import project.domain.cafe.collection.MongoCollectionRepository
import project.domain.cafe.topic.MongoTopicRepository
import project.domain.cafe.topic.TopicSubunit
import project.domain.profile.MongoProfileRepository
import project.domain.profile.ProfileSubunit
import project.domain.session.MongoSessionStore
import project.domain.session.WebsiteSessionSubunit

/**
 * Create and return a [ServerContext] instance.
 */
suspend fun createServerContext(
    appScope: CoroutineScope,
    serverSubunitScope: ServerScope,
    collectionName: MongoCollectionName,
    mongoDatabase: MongoDatabase
): ServerContext {
    // setup ServerContext
    val dataStore = MongoDataStore(
        db = mongoDatabase,
        collectionName = collectionName
    ).also { it.awaitInit() }
    val accountRepository = MongoAccountRepository(
        accountCollection = mongoDatabase.getCollection(collectionName.userAccount)
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

    val profileRepository = MongoProfileRepository(
        accountCollection = mongoDatabase.getCollection(collectionName.userAccount)
    )
    val profileSubunit = ProfileSubunit(profileRepository)

    val userCreationSubunit = UserCreationSubunit(dataStore)
    val authSubunit = AuthSubunit(accountSubunit, userCreationSubunit)

    val sessionStore = MongoSessionStore(mongoDatabase.getCollection(collectionName.websiteSession))

    val topicRepository = MongoTopicRepository(
        topicCollection = mongoDatabase.getCollection(collectionName.topic)
    ).also { it.awaitInit() }
    val collectionRepository = MongoCollectionRepository(
        spaceCollection = mongoDatabase.getCollection(collectionName.spaces),
        sectionCollection = mongoDatabase.getCollection(collectionName.sections)
    )

    val websiteSession = WebsiteSessionSubunit(appScope, TimeCenter.source, sessionStore)
    val topicSubunit = TopicSubunit(topicRepository)
    val collectionSubunit = CollectionSubunit(collectionRepository)

    val subunits = ServerSubunits(
        account = accountSubunit,
        presence = userPresenceSubunit,
        auth = authSubunit,
        session = sessionSubunit,
        creation = userCreationSubunit,

        websiteSession = websiteSession,
        profile = profileSubunit,
        topic = topicSubunit,
        collection = collectionSubunit
    )

    // debut all subunits
    subunits.debut(serverSubunitScope)

    val serverContext = ServerContext(
        dataStore = dataStore,
        stageActDirector = stageActDirector,
        commandDispatcher = commandDispatcher,
        webSocketManager = webSocketManager,
        subunits = subunits
    )

    return serverContext
}
