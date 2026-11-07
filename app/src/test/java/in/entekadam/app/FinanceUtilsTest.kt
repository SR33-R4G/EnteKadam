package in.entekadam.app

import in.entekadam.app.util.parsePaise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceUtilsTest {
    @Test fun parsesRupeesToExactPaise() {
        assertEquals(123456L, parsePaise("1,234.56"))
        assertEquals(100L, parsePaise("1"))
        assertEquals(5L, parsePaise("0.05"))
    }
    @Test fun rejectsInvalidOrOverPrecisionValues() {
        assertNull(parsePaise("abc"))
        assertNull(parsePaise("1.001"))
    }
}
