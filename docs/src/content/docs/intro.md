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
  [Lounge]
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
└─ Space
   └─ Section
       └─ Topic
            └─ Reply
```

The atomic unit of the cafe system is _topic_. It represents a single forum post. User create a topic with a title and content. Each post within the topic is considered a reply. The author of the topic is considered as the first reply. Other users can make reply too.

The forum will be divided into _spaces_, then _sections_, and finally individual topics.

- Space: a group of discussions with similar subject. This is imagined as a real spot in a cafe.
  - e.g., lounge, bias corner, terrace
- Section: discussion subject within a space; a _collection of topics_. This does not relate to any location terminology. A section restrict users to only discuss about the relevant subject.
  - e.g.,
    - Kep1er Discussion, discussion related to Kep1er. Others K-pop group discussion shouldn't belong here.
    - Yujin's Space, discussion related to Yujin; e.g., solo activities, talk directly related to Yujin. Other members' discussions shouldn't belong here unless related or talk about Yujin specifically. If it's more like a group activity, it may belong to Kep1er Discussion instead.
    - Media and Games, off-topic discussion; e.g., other media culture and gaming discussions.
- Topic: a single forum post. They are exclusively within a section.
  - e.g., the topic1 with title "Oh, Yujin is so pretty..."
- Reply: unit of content within a topic.
  - e.g., reply1 in topic1 with content "I think I have fallen for her..."

The internal name does not need to reflect the cosmetic name used in the application. e.g., "Yujin's Space" when technically it's a section.

Section and space will be stored in a separate collection in the database from the topics. Technically, opening the cafe page will retrieve the available spaces, group them within all the sections, and fetch the latest topics from each section.

Section and space won't likely be created often, so the server should cache them in memory to avoid repeated DB query. In reality, creation could be once every few months or even never. Though, they should still be stored in the DB instead of hardcoded in the app to avoid administrator editing the code just to create new. This makes it possible to implement a "create new section" feature.

Space and section will be a very tiny collection. We can model it like:

```json
spaces: [
  {
    id: "lounge",
    name: "Lounge",
    order: 0
  },
  {
    id: "bias-corner",
    name: "Bias Corner",
    order: 1
  },
  {
    id: "terrace",
    name: "Terrace (off-topic)",
    order: 2
  }
]
```

Order is a numerical value that will determine the cafe layout display. The `id` here is merely for referential purpose. It isn't shown in the cafe application. User either open the cafe which shows every space and sections within them, or open individual sections to see every topics.

```json
sections: [
  {
    id: "kep1er",
    spaceId: "lounge",
    name: "Kep1er Discussion",
    order: 0
  },
  {
    id: "kpop",
    spaceId: "lounge",
    name: "K-pop Discussion",
    order: 1
  },
  {
    id: "yujin",
    spaceId: "bias-corner",
    name: "Yujin's Space",
    order: 0
  },
  {
    id: "xiaoting",
    spaceId: "bias-corner",
    name: "Xiaoting's Space",
    order: 1
  },
  {
    id: "media",
    spaceId: "terrace",
    name: "Media",
    order: 0
  },
  {
    id: "games",
    spaceId: "terrace",
    name: "Games",
    order: 1
  }
]
```

Order value of each section represents their order on their respective space; e.g., "Xiaoting's Space" is ordered as the second within the "bias_corner" space, preceded by "Yujin's Space" which is the first.

Unlike space, section's `id` will be displayed to users. It will be used as the forum URL. Therefore, the `id` shouldn't contain any special characters. It should also be manually created instead of produced from section's name. This avoid long URL just because the section's name is long.

For example, clicking "Yujin's Space" in the cafe homepage will redirect user to `/cafe/yujin`.

We can model topic like:

```json
topics: [
  {
    topicId: "123e4567-e89b-12d3-a456-426614174000",
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

A request to `/cafe/yujin` would filter every topics of `spaceId == "yujin"`. On a bigger scale though, maybe topics should be partitioned into their respective section. This results in multiple collections of topics grouped by their section.

The `topicId` must be unique and can rely on UUID. The first 8-characters will also be used for URL generation of the topic. In this case, the server should check for possible collision of the first 8-characters of the newly generated UUID.

More specifically, the URL of a topic will be a combination of the topic's title and its unique ID.

For example, `forumdomain.com/cafe/yujin/123e4567/oh-yujin-is-so-pretty`:

- `forumdomain.com` the domain name of the social portal website.
- `cafe` represent the cafe section of the website.
- `yujin` the section identifier `yujin`.
- `123e4567` is the first 8-characters of the `topicId`.
- `oh-yujin-is-so-pretty` is a Base62 (a-z, A-Z, 0-9) encoded string of the topic's title.

Key points:

- The topic title is included in the URL for UX. This will let users know the rough subject of a forum post just by seeing the URL. This is also often called as _slug_.
- The purpose of including UUID in the URL is to prepare for potential URL change. The UUID is only the first 8-characters to avoid long URL.
  - User can edit the topic's title. When it is edited, a new URL will be produced.
  - The goal is to prevent dead links. When users open the old URL, they should be redirected to the new URL.
  - The server can achieve this by using the UUID for identification.
  - It would retrieve the specific topic associated with this UUID, identified by the first 8-characters.
  - An actual dead link occurs when there are no matching IDs.
  - There are 4.2 billion unique combinations from the first 8-characters of UUID. Though, collision can still occur, so server should re-generate until the first 8-characters are different.

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

### Session

The session model used in the website is as follow:

- A session is identified with a UUID.
- It has `expiresAt` which is the timestamp when it will expire.
- A session will last for 365 days, and this can be refreshed every 30 days.
- User that register or login will create a new session, this is stored as cookie in local storage.
- The server will store every user sessions in the memory and in MongoDB as backup. The memory remain as the main operator.

Some routes require cookie check and some do not. There should be three models:

- NoAuthGuard: nothing related to auth is checked, every requests pass through
- OptionalAccount: this only check the cookie; used for most website routes to create greeting message like "Welcome back, Alice" or "Logged in as Alice"
- RequireAccount: this is for routes where account is required, such as posting a cafe post, editing own's profile, messaging other users.

Refresh behavior:
- Session can be refreshed unlimited times.
- Refresh is done when at least 30 days passed on expiresAt (e.g., <= 335 days)
- Server will also periodically clean token session, maybe every 1 day, if the token is no longer valid (more than 365 days)

The behavior:

- user register/login, a session is generated which will be valid for the next 365 days. the server also return a cookie which is saved in user local storage
- for each request to any routes, the cookie is sent (use path '/')
- if cookie not exist, this mean user hasn't logged in.
     - in this case, server returns "not logged in" text in the template
- if cookie exist
     - server check if that cookie is valid (i.e., exist in the store) and it hasn't expire
     - if cookie is valid
        - server returns "logged in" text in the template
        - user can do any auth required action (e.g., create new post)
        - if there is at least 30 days passed on expiresAt (e.g., <= 335 days), server will reset it back to 365 days.
     - if cookie is invalid (expired)
        - server returns "not logged in" text in the template
        - the session saved in the server will also be deleted, if exists

Other case:

- when user log out, the session will be deleted
- when user login again, maybe in another device, new session is created normally

Also need persistence backup with mongodb.

- on server startup, load the persistented session data
- on new session created (each register/login) run mongodb insertone
- on session refresh run mongodb updateone
- on session deleted (user logout) run mongodb deleteone
- data model should be {sessionId: expiresAt}
- do a periodic cleanup which delete every expired session (expiresAt is more than now millis)

for example:

- user first time visit on website "not logged in" (OptionalAccount guard)
- user register an account and now see "logged in as ..." (session is created, inserted to mongodb, valid for 365 days)
- user open forum section, they still see "logged in as ..." (OptionalAccount guard)
- user never open the website again until the next 10 days, still see "logged in as ..." (OptionalAccount guard, remaining session 355 days)
- user never open the website again until the next 21 days, still see "logged in as ..." (OptionalAccount guard, remaining session 334 days, refreshed back to 365 days)
- user never open the website again until the next 365 days, cookie may still exist in the browser, but it's now invalid. so user have to login again.
