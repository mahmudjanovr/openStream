package com.openstream.app.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlValidatorTest {

    @Test
    fun isValidMediaUrl_validHttpAndHttps_returnsTrue() {
        assertTrue(MediaUrlValidator.isValidMediaUrl("https://archive.org/download/his_girl_friday/his_girl_friday.mp4"))
        assertTrue(MediaUrlValidator.isValidMediaUrl("http://archive.org/download/thegeneral/thegeneral.mp4"))
    }

    @Test
    fun isValidMediaUrl_validLocalFile_returnsTrue() {
        assertTrue(MediaUrlValidator.isValidMediaUrl("/data/user/0/com.openstream.app/files/openstream_downloads/movie.mp4"))
        assertTrue(MediaUrlValidator.isValidMediaUrl("file:///storage/emulated/0/Android/data/com.openstream.app/files/movie.mp4"))
    }

    @Test
    fun isValidMediaUrl_invalidOrMaliciousUrl_returnsFalse() {
        assertFalse(MediaUrlValidator.isValidMediaUrl(null))
        assertFalse(MediaUrlValidator.isValidMediaUrl(""))
        assertFalse(MediaUrlValidator.isValidMediaUrl("javascript:alert(1)"))
        assertFalse(MediaUrlValidator.isValidMediaUrl("https://example.com/<script>alert(1)</script>"))
        assertFalse(MediaUrlValidator.isValidMediaUrl("/data/user/0/../../etc/passwd"))
    }
}
