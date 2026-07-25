package testUtils

import project.mongo.collection.UserAccount
import project.mongo.collection.UserId
import encore.time.TimeCenter
import encore.utils.hash
import project.domain.profile.Profile

fun createAccount(userId: UserId, username: String, password: String): UserAccount {
    val now = TimeCenter.now()
    return UserAccount(
        userId = userId,
        username = username,
        email = "$username@email.com",
        hashedPassword = hash(password),
        registeredAt = now,
        lastActiveAt = now,
        extra = emptyMap(),
        profile = createProfile()
    )
}

fun createProfile(): Profile {
    return Profile(
        displayName = "",
        avatarUrl = "",
        level = 1
    )
}
