package project.dummy

/**
 * Provides reusable nouns, verbs, and adjectives.
 */
object Words {
    private val nouns = listOf(
        "orange", "tomato", "veggies", "handphone", "gadget",
        "sandwich", "tank", "burger", "chicken", "cow",
        "hotdog", "water", "lily", "glass", "coffee",
        "beef", "chili", "roof", "zombie", "car",
    )

    private val verbs = listOf(
        "love", "like", "bias", "admire", "support",
        "adore", "appreciate", "care about", "respect", "value",
    )

    private val adjectives = listOf(
        "cute", "talented", "beautiful", "lovely", "best",
        "smart", "classy", "dazzling", "gorgeous", "cool",
        "pretty", "kind", "ambitious", "bright", "clever",
        "perfect", "elegant", "radiant", "graceful", "nice",
    )

    fun noun(): String = nouns.random()
    fun capitalNoun(): String = nouns.random().capitalizeFirstLetter()

    fun verb(): String = verbs.random()
    fun capitalVerb(): String = verbs.random().capitalizeFirstLetter()

    fun adjective(): String = adjectives.random()
    fun capitalAdjective(): String = adjectives.random().capitalizeFirstLetter()

    private fun String.capitalizeFirstLetter(): String {
        require(this.isNotBlank())
        return this.first().uppercase() + this.substring(1)
    }
}
