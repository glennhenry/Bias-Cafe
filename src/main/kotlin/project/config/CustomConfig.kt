package project.config

import encore.annotation.runtime.VenueKey

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
     * Whether to fabricate dummy activities in the website.
     * This includes creating dummy accounts, topics, replies, etc.
     * This will only be done if the accounts database has fewer than 5 accounts.
     */
    @VenueKey("setupDummyActivity")
    val setupDummyActivity: Boolean = false,
)
