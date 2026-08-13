package portal.domain.cafe.view.model

import portal.routes.common.AccountData

/**
 * Model for the cafe landing page: `/cafe`.
 *
 * @property account Optional [AccountData] of the user.
 * @property spaces A unit that groups together related sections in the cafe.
 * @property topicCounts The amount of topic that exists on each section.
 */
data class CafeModel(
    val account: AccountData?,
    val spaces: List<SpaceItem>,
    val topicCounts: Map<String, Int>
)

/**
 * Describe a single space.
 *
 * @property name The name of the space (e.g., "Lounge", "Bias Corner", "Terrace")
 * @property sections The sections that this space contains.
 */
data class SpaceItem(
    val name: String,
    val sections: List<SectionItem>
)

/**
 * Describe a single cafe section.
 *
 * @property id Unique identifier of the section. This is used for page's slug.
 * @property name Display name of the section (e.g., "Kep1er Discussion")
 * @property description Text describing the purpose of the section.
 */
data class SectionItem(
    val id: String,
    val name: String,
    val description: String
)
