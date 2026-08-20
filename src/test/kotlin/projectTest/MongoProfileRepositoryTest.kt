package projectTest

import TestCollections
import encore.utils.hash
import initMongo
import kotlinx.coroutines.test.runTest
import portal.domain.profile.subunits.MongoProfileRepository
import portal.domain.profile.model.Profile
import testUtils.createAccount
import testUtils.createProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test operations of [MongoProfileRepository].
 */
class MongoProfileRepositoryTest {
    @Test
    fun `test all`() = runTest {
        val mongoDb = initMongo()
        val collection = mongoDb.getCollection<Profile>(TestCollections.profiles)
        collection.drop()
        mongoDb.createCollection(TestCollections.profiles)

        val repo = MongoProfileRepository(collection)

        // setup
        val id = "5ab0980c-e2cb-990a-427a-5ad9b0311b7f"
        val targetProfile = createProfile(
            userId = id,
            displayName = "UserABC",
            avatarUrl = "avatars/duck.jpg"
        )
        val targetProfile2 = createProfile(
            userId = "otherId",
            displayName = "otherDisplayName"
        )
        collection.insertMany(
            listOf(targetProfile) + targetProfile2 + List(10) { createProfile() }
        )

        // tests
        // 1. getProfile
        assertEquals("UserABC", repo.getProfile(id).getOrNull()?.displayName)

        // 2. insert
        repo.insert(createProfile(userId = "xyzasdf"))
        assertEquals("xyzasdf", repo.getProfile("xyzasdf").getOrNull()?.userId)

        // 3. getUserSummary
        assertTrue {
            val x = repo.getUserSummary(id).getOrThrow()
            x?.userId == id && x.displayName == "UserABC" && x.avatarUrl == "avatars/duck.jpg"
        }

        // 4. getUserSummaries
        assertTrue {
            val x = repo.getUserSummaries(listOf(id, "otherId")).getOrThrow()
            x[id]?.displayName == "UserABC" && x[id]?.avatarUrl == "avatars/duck.jpg" && x["otherId"]?.displayName == "otherDisplayName"
        }

        // 5. getProfileOverview
        assertEquals(targetProfile.birthday, repo.getProfileOverview(id).getOrThrow()?.birthday)

        // 5. getFanProfile
        assertEquals(targetProfile.fanProfile.favoriteSong, repo.getFanProfile(id).getOrThrow()?.favoriteSong)
    }
}

