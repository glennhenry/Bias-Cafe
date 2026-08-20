package testUtils

import portal.mongo.collection.UserAccount
import portal.mongo.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import portal.domain.Members
import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.GameProfile
import portal.domain.profile.model.Profile
import portal.domain.profile.model.UserLevel
import portal.domain.profile.model.UsersStats

fun createAccount(
    userId: UserId = Ids.uuid(),
    username: String = randomString(8),
    displayName: String = randomString(8),
    password: String = randomString(8)
): UserAccount {
    val now = TimeCenter.now()
    return UserAccount(
        userId = userId,
        username = username,
        displayName = displayName,
        email = "$username@email.com",
        hashedPassword = hash(password),
        registeredAt = now,
        lastActiveAt = now,
        extra = emptyMap(),
    )
}

fun createProfile(
    userId: UserId = Ids.uuid(),
    displayName: String = randomString(8),
    avatarUrl: String = randomString(8)
): Profile {
    return Profile(
        userId = userId,
        displayName = displayName,
        avatarUrl = avatarUrl,
        country = "Indonesia",
        birthday = "20220103",
        bio = "Catch your eye",
        fanProfile = FanProfile(
            startedStan = "3 January 2022",
            favoriteSong = "WA DA DA",
            favoriteEra = "First Impact",
            bias = Members.all.toList(),
            story = "I like em"
        ),
        gameProfile = GameProfile(
            level = UserLevel(
                currentLevel = 1,
                xp = 10
            ),
            coins = 100,
            badges = emptyList(),
            achievements = emptyList()
        ),
        blockedUsers = emptyList(),
        stats = UsersStats(
            numTopics = 0,
            numReplies = 0
        ),
    )
}
