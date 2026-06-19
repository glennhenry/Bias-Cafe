---
title: Intro
slug: index
description: Intro
---

# Bias Cafe

Documentation about bias cafe.

Fan social project application.

17/06/2026.

## Scratchpad

The site is like a social universe of Kep1er. It is RPG-ed social media.

Has:

- forum for discussion.
  - member section containing 9 boards for each member,
  - 1 section for general,
  - 1 section off topic, etc.
  - forum is gamified with badge, ranking, level.
  - forum looks like old school like ragezone
  - reply is a post too, has reaction for comment
  - post has like or dislike, has view
  - reputation system
  - has tags for category and topics.
- Widgets:
  - schedule countdown
  - Chart counter
  - member birthday
  - current user birthday
  - polls
  - most popular member's forrum
  - hot posts
  - latest board message
  - top user today/week/month/alltime
  - new users
- board for one-way announcement from member to fan
  - forum is for discussion while board is like annuncement from official or member
- profile system
  - profile is used in forum
  - has mailbox for direct message and web notiifcation
  - has guestbook, leaving public message for a user
  - has description, profile picture, bias, level, title, mood/status
- quest system
  - daily quest, account quest
  - e.g., read 5 posts today.
  - e.g., assign your bias, comment in your bias forum, etc.
  - mission gives badge
  - quest try to not gives dead activity like reaction, post, this can mirror spam behavior.
- calendar system
  - login attendance , schedule
- badge system
  - badge is like achievement which is also collectibles
  - complete quests, login on specific day (special era's badge),
- scrapbook
  - user's activity storage, post, view history
  - liked posts, reacted comment, bookmarked post
- dynamic theme system
  - theme is switcahble to different comeback era
  - debut (light theme, dark purple accent)
  - killa (dark theme, dark red accent)
- Level system
  - for example, login gives 10 XP, complete quest. gain xp from post upvote
  - level up gives cash
  - cash use for buying profile border, title.
  - richest user, highest level user
- photocard system
  - photocards are collectibles
  - they are put in profile alongside badge
  - badges are quests only, while photocard can be bought through gacha
  - display single photocard on forum profile

- Homepage should look like a project, aggregating various content such as forum board, birthday,

## Terminology

### Cafe (forum)

The cafe is structured like:

```
Cafe
  [Living Area]
    Kep1er Discussion
    K-pop Discussion

  [Bias Corner]
    Yujin's Space
      topic1:
          title: "Oh, Yujin is so pretty..."
          author: "UtokkiForever"
          reply1:
            author: "UtokkiForever"
            content: "I think I have fallen for her..."
          reply2
            author: "ThinkingInXiao"
            content: "Yeah she is!"
      topic2:
          title: "Yujin's Fancam Collection Help"
          author: "UtokkiForever"
          reply1:
            author: "UtokkiForever"
            content: "I need help finding more fancam of her"
          reply2
            author: "Yujiniee"
            content: "I have a tons! Send me a letter."
    Xiaoting's Space

  [Terrace]
    Media
    Games

Cafe
└─ Section
   └─ Space
       └─ Topic
            └─ Reply
```

- Section: a group of related discussions;
  - e.g., living area, bias corner, terrace
- Space: single discussions topic;
  - e.g., Kep1er Discussion, Yujin's Space, Media
- Topic: single post within a discussion;
  - e.g., the post1 with title "Oh, Yujin is so pretty..."
- Reply: unit of content within a topic, where the author is the first reply itself;
  - e.g., reply1 in post1 with content "I think I have fallen for her..."

Technically, we would model it like:

```
topics: [
  {
    topicId: "xxxx-yyyy",
    spaceId: "yujin",
    title: "Oh, Yujin is so pretty...",
    author: "UtokkiForever",
    replies: [
      {
        author: "UtokkiForever"
        content: "I think I have fallen for her..."
      },
      {
        author: "ThinkingInXiao"
        content: "Yeah she is!"
      }
    ]
  }
]
```

Section and space is not stored in database and located in code.

```kotlin
enum class SectionId {
    LIVING_AREA,
    BIAS_CORNER,
    TERRACE
}

data class Space(
    val id: Int,
    val sectionId: SectionId,
    val name: String
)

val spaces = listOf(
    Space(0, LIVING_AREA, "Kep1er Discussion"),
    Space(1, LIVING_AREA, "K-pop Discussion"),

    Space(2, BIAS_CORNER, "Yujin's Space"),
    Space(3, BIAS_CORNER, "Xiaoting's Space"),

    Space(4, TERRACE, "Media"),
    Space(5, TERRACE, "Games")
)
```

A page request to Yujin's space (e.g., `/cafe/yujin`) would filter the available topics of `spaceId == "yujin"`.

### Mailbox (private message)

The mailbox is private message system where two individuals can send each other message called **letter**.

### Bulletin Board (announcement)

The bulletin board is a one-way channel from approved entity such as the system, official, or each individual idols.

It is a flat list of content, where a single unit is called **post**

For example:

```
[2026-06-19 12:31:12]: New post on bulletin board posted by "Yujin" (Hello Kep1lians!)
[2026-06-18 12:31:12]: New post on bulletin board posted by "System" (Hotfix version 1.3.4)
[2026-06-17 12:31:12]: New post on bulletin board posted by "Kep1er" (Schedule announcement)
```
