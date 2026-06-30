package project.config

import encore.annotation.runtime.VenueKey
import project.domain.cafe.topic.MongoTopicRepository

/**
 * Custom application config definition.
 *
 * Place every venue-supplied configuration here, also annotate with [VenueKey].
 * After that, modify `venue.xml` accordingly from this data class entries.
 *
 * All field is preferred to be immutable.
 */
data class CustomConfig(
    /**
     * Whether to prepare dummy posts for [MongoTopicRepository].
     *
     * This will insert 20 posts during server startup if
     * fewer than 10 posts are available.
     */
    @VenueKey("prepareDummyPosts")
    val prepareDummyPosts: Boolean = false,

    /**
     * Whether to delete all stored posts in the topic collection
     * of [MongoTopicRepository] on every server restart.
     *
     * Deletion will be done first before adding the posts
     * if [prepareDummyPosts] is `true`.
     */
    @VenueKey("deletePostsEveryRestart")
    val deletePostsEveryRestart: Boolean = false
)
