package portal.context

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.account.AccountSubunit
import encore.account.MongoAccountRepository
import encore.acts.ActIdStore
import encore.acts.StageActDirector
import encore.auth.AuthSubunit
import encore.backstage.command.CommandDispatcher
import encore.context.ContextFactory
import encore.creation.UserCreationSubunit
import encore.datastore.MongoDataStore
import encore.presence.UserPresenceSubunit
import encore.session.SessionSubunit
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.websocket.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import portal.RealUserCreationFactory
import portal.domain.cafe.collection.CollectionSubunit
import portal.domain.cafe.collection.MongoCollectionRepository
import portal.domain.cafe.topic.MongoTopicRepository
import portal.domain.cafe.topic.TopicSubunit
import portal.domain.cafe.reply.MongoReplyRepository
import portal.domain.cafe.reply.ReplySubunit
import portal.domain.profile.subunits.MongoProfileRepository
import portal.domain.profile.subunits.ProfileSubunit
import portal.domain.auth.session.MongoSessionStore
import portal.domain.auth.session.WebsiteSessionSubunit
import portal.mongo.MongoCollections

/**
 * Real implementation of [ContextFactory].
 *
 * Context creation here is user-owned and must be updated accordingly.
 *
 * @property collections Mongo collection names
 * @property mongoDatabase Mongo database
 */
class RealContextFactory(
    private val collections: MongoCollections,
    private val mongoDatabase: MongoDatabase
): ContextFactory {
    override suspend fun serverContext(
        appScope: CoroutineScope,
        serverSubunitScope: ServerScope
    ): ServerContext {
        /*... setup ServerContext ...*/

        val dataStore = MongoDataStore(
            db = mongoDatabase,
            collections = collections
        ).also { it.awaitInit() }

        val accountRepository = MongoAccountRepository(
            accountCollection = mongoDatabase.getCollection(collections.userAccount)
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
            profiles = mongoDatabase.getCollection(collections.profiles)
        )
        val profileSubunit = ProfileSubunit(profileRepository)

        val userCreationSubunit = UserCreationSubunit(dataStore, profileRepository, RealUserCreationFactory())
        val authSubunit = AuthSubunit(accountSubunit, userCreationSubunit)

        val sessionStore = MongoSessionStore(mongoDatabase.getCollection(collections.websiteSession))

        val topicRepository = MongoTopicRepository(
            topicCollection = mongoDatabase.getCollection(collections.topic)
        ).also { it.awaitInit() }
        val replyRepository = MongoReplyRepository(
            replies = mongoDatabase.getCollection(collections.reply)
        ).also { it.awaitInit() }
        val collectionRepository = MongoCollectionRepository(
            spaceCollection = mongoDatabase.getCollection(collections.spaces),
            sectionCollection = mongoDatabase.getCollection(collections.sections)
        )

        val websiteSession = WebsiteSessionSubunit(appScope, TimeCenter.source, sessionStore)
        val topicSubunit = TopicSubunit(topicRepository)
        val replySubunit = ReplySubunit(replyRepository)
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
            reply = replySubunit,
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
}
