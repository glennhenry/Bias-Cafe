package portal.domain.dummy

import portal.domain.Members
import portal.domain.profile.model.FanProfile
import portal.domain.profile.model.GameProfile
import portal.domain.profile.model.Profile
import portal.domain.profile.model.UserLevel
import portal.domain.profile.model.UsersStats
import portal.mongo.collection.UserId
import java.text.SimpleDateFormat
import kotlin.random.Random

object ProfileFactory {
    private val avatars = listOf(
        "avatars/duck.jpg",
        "avatars/penguin.jpg",
        "avatars/truck.jpg",
        "avatars/turtle.jpg",
        "avatars/work.jpg"
    )

    private val countries = listOf(
        "Canada", "United States", "Brazil", "United Kingdom", "France",
        "Germany", "Russia", "Turkey", "India", "China",
        "Japan", "South Korea", "Thailand", "Indonesia", "Australia"
    )

    fun profile(userId: UserId, displayName: String): Profile {
        val fanProfile = fanProfile()
        return Profile(
            userId = userId,
            displayName = displayName,
            avatarUrl = avatars.random(),
            country = countries.random(),
            birthday = formatter.format(birthday()),
            bio = bio(displayName = displayName, fanProfile.bias.first()),
            fanProfile = fanProfile,
            gameProfile = gameProfile(),
            blockedUsers = emptyList(),
            stats = stats()
        )
    }

    private val formatter = SimpleDateFormat("dd MMMM yyyy")
    private val birthdayRange = listOf(
        // 1 Jan 1980 - 1 Jan 1990
        LongRange(315532800000, 631152000000),
        // 1 Jan 1990 - 1 Jan 1995
        LongRange(631152000000, 788918400000),
        // 1 Jan 1995 - 1 Jan 2000
        LongRange(788918400000, 946684800000),
        // 1 Jan 2000 - 1 Jan 2010
        LongRange(946684800000, 1262304000000),
        // 1 Jan 2010 - 1 Jan 2026
        LongRange(1262304000000, 1767225600000),
    )

    // e.g., 50% change for someone to born somewhere between 2000-2010
    fun birthday(): Long {
        return when (Random.nextDouble()) {
            in 0.0..<0.05 -> birthdayRange[0]
            in 0.05..<0.15 -> birthdayRange[1]
            in 0.15..<0.45 -> birthdayRange[2]
            in 0.45..<0.95 -> birthdayRange[3]
            in 0.95..1.0 -> birthdayRange[4]
            else -> birthdayRange[3]
        }.random()
    }

    private val bio = listOf(
        "Hello, I'm {1}. I bias {2} the most. She is the prettiest!",
        "I love {2} than anyone else (including my girlfriend 😀😀😀 hahahahahahahaha)",
        "I'm new in the cafe",
        "whatever",
        "...why are you reading this?",
        "Insert your bio here 💩💩💩",
        "kep1er best group ever",
        "lorem ipsum dolor sit amet 💤💤",
        "everybody should stan kep1er 😎",
        "I like to downvote people post in random 😀😀😀",
    )

    fun bio(displayName: String, memberName: String): String {
        return bio.random().replace("{1}", displayName).replace("{2}", memberName)
    }

    private val story = listOf(
        "Kep1er is my favorite K-pop group, I love them.",
        "I first got interested to {1}, now I love everybody.",
        "Kep1er OT9 forever ❤️❤️",
        "I just know them yesterday. Yes, they are my favorite now.",
        ""
    )

    private val songs = listOf(
        "WA DA DA", "UP", "We Fresh", "Wing Wing", "I do do you",
        "Giddy", "Galileo", "Grand Prix", "Straight Line", "Shooting Star",
        "TIPI-TAP", "Yum", "Bubble Gum", "KILLA"
    )

    private val eras = songs

    fun bias(): List<String> {
        // 30% chance for 9 biases
        if (Random.nextDouble() < 0.3) {
            return Members.all.toList()
        }
        // 40% chance for 2 biases
        // 30% chance for 3 biases, and so on...
        val numBias = when (Random.nextDouble()) {
            in 0.0..<0.4 -> 2
            in 0.4..<0.6 -> 3
            in 0.6..<0.7 -> 4
            in 0.7..<0.8 -> 5
            in 0.8..<0.9 -> 6
            in 0.9..<0.95 -> 7
            in 0.95..1.0 -> 8
            else -> 2
        }
        return Members.all.shuffled().take(numBias)
    }

    fun fanProfile(): FanProfile {
        val bias = bias()
        return FanProfile(
            bias = bias,
            favoriteSong = songs.random(),
            favoriteEra = eras.random() + " Era",
            story = story.random().replace("{1}", bias.first())
        )
    }

    fun gameProfile(): GameProfile {
        return GameProfile(
            level = UserLevel(1, 0),
            coins = 100,
            badges = emptyList(),
            achievements = emptyList()
        )
    }

    fun stats(): UsersStats {
        return UsersStats(
            numTopics = 0,
            numReplies = 0
        )
    }
}
