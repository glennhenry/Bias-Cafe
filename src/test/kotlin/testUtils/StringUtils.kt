package testUtils

fun randomString(length: Int, pool: List<Char> = ('a'..'z').toList()): String {
    return buildString(length) {
        repeat(length) { append(pool.random()) }
    }
}
