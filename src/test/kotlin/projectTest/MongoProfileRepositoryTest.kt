package projectTest

import TestCollections
import encore.utils.hash
import initMongo
import kotlinx.coroutines.test.runTest
import project.domain.profile.MongoProfileRepository
import project.domain.profile.Profile
import project.mongo.collection.UserAccount
import testUtils.createAccount
import testUtils.createProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test operations of [project.domain.profile.MongoProfileRepository].
 */
class MongoProfileRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<UserAccount>(TestCollections.userAccount)
        collection.drop()
        mongoDb.createCollection(TestCollections.userAccount)

        val repo = MongoProfileRepository(collection)

        // setup
        val id = "5ab0980c-e2cb-990a-427a-5ad9b0311b7f"
        val targetAcc = UserAccount(
            userId = id,
            username = "user123",
            email = "email123@a",
            hashedPassword = hash("yesyes"),
            registeredAt = 0,
            lastActiveAt = 0,
            extra = emptyMap(),
            profile = Profile(
                displayName = "UserABC",
                avatarUrl = "avatars/duck.jpg",
                level = 1
            )
        )
        val targetAcc2 = createAccount(
            userId = "otherId",
            profile = createProfile(displayName = "otherDisplayName")
        )
        collection.insertMany(
            listOf(targetAcc) + targetAcc2 + List(10) { createAccount() }
        )

        // tests
        // 1. getProfile
        assertEquals("UserABC", repo.getProfile(id).getOrNull()?.displayName)

        // 2. getUserSummary
        assertTrue {
            val x = repo.getUserSummary(id).getOrThrow()
            x?.userId == id && x.displayName == "UserABC" && x.avatarUrl == "avatars/duck.jpg"
        }

        // 3. getUserSummaries
        assertTrue {
            val x = repo.getUserSummaries(listOf(id, "otherId")).getOrThrow()
            x[id]?.displayName == "UserABC" && x["otherId"]?.displayName == "otherDisplayName"
        }
    }
}

