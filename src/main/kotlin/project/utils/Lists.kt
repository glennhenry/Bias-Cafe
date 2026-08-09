package project.utils

fun <T> List<T>.peek(amount: Int): List<T> {
    val outSize = minOf(amount, size)
    return subList(0, outSize - 1)
}
