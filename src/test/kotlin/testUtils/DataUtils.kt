package testUtils

import project.mongo.collection.UserAccount
import project.mongo.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash
import encore.utils.identifier.Ids
import project.domain.profile.Profile

fun createAccount(
    userId: UserId = Ids.uuid(),
    username: String = randomString(8),
    password: String = randomString(8),
    profile: Profile = createProfile()
): UserAccount {
    val now = TimeCenter.now()
    return UserAccount(
        userId = userId,
        username = username,
        email = "$username@email.com",
        hashedPassword = hash(password),
        registeredAt = now,
        lastActiveAt = now,
        extra = emptyMap(),
        profile = profile
    )
}

fun createProfile(
    displayName: String = randomString(8),
    avatarUrl: String = randomString(8)
): Profile {
    return Profile(
        displayName = displayName,
        avatarUrl = avatarUrl,
        level = 1
    )
}
