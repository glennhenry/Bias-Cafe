package projectTest

import project.domain.cafe.toUrlSlug
import kotlin.test.Test
import kotlin.test.assertEquals

class UrlUtilsTest {
    @Test
    fun `test toUrlSlug`() {
        assertEquals("oh-yujin-is-so-pretty", "Oh, Yujin is so pretty!".toUrlSlug())
        assertEquals("help-me-10", "Help me-10 💀 김".toUrlSlug())
        assertEquals("-", "----------".toUrlSlug())
        assertEquals("-", "-!#@!#!@$".toUrlSlug())
        assertEquals("-", "김유진".toUrlSlug())
        assertEquals("-", "😀😀😀".toUrlSlug())
        assertEquals("hello-world", "___Hello___World___".toUrlSlug())
        assertEquals("a-b", "a---b".toUrlSlug())
        assertEquals("hello-world", "Hello     World".toUrlSlug())
        assertEquals("hello", "  Hello  ".toUrlSlug())
    }
}
