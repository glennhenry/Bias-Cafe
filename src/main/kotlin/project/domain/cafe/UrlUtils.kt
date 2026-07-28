package project.domain.cafe

/**
 * Converts this string into a URL slug.
 *
 * The resulting slug contains only lowercase ASCII letters (`a-z`),
 * digits (`0-9`), and hyphens (`-`). Any other characters, including
 * whitespace, punctuation, emojis, and non-ASCII characters, are
 * replaced with hyphens. Consecutive hyphens are collapsed into one.
 * No leading or trailing hyphens are allowed.
 *
 * Examples:
 * ```
 * str: "Oh, Yujin is so pretty!"
 * out: "oh-yujin-is-so-pretty"
 *
 * str: "Help me-10 💀 김"
 * out: "help-me-10"
 *
 * str: "😀😀😀"
 * out: "-"
 * ```
 */
fun String.toUrlSlug(): String {
    val sb = StringBuilder()
    for ((i, c) in this.withIndex()) {
        when (c.code) {
            // 0-9
            in 48..57 -> sb.append(c)
            // A-Z
            in 65..90 -> sb.append(c.lowercaseChar())
            // a-z
            in 97..122 -> sb.append(c)
            // - and everything else
            else -> {
                if (sb.isNotEmpty() && sb.last() != '-') {
                    sb.append('-')
                }
            }
        }
    }
    if (sb.isEmpty()) {
        return "-"
    }
    if (sb.last() == '-') {
        return sb.deleteCharAt(sb.lastIndex).toString()
    }
    return sb.toString()
}
