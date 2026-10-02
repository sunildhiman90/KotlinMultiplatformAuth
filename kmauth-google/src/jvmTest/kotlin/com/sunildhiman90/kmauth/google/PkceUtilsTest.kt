package com.sunildhiman90.kmauth.google

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PkceUtilsTest {

    @Test
    fun testGenerateCodeVerifierProducesValidString() {
        val verifier = PkceUtils.generateCodeVerifier()

        // RFC 7636 Section 4.1: minimum length 43 characters, maximum length 128 characters
        assertTrue(
            verifier.length in 43..128,
            "Verifier length ${verifier.length} should be between 43 and 128 characters"
        )

        // RFC 7636 Section 4.1: unreserved characters [A-Z], [a-z], [0-9], "-", ".", "_", "~"
        // Base64URL without padding produces [A-Za-z0-9_-]
        val validBase64UrlPattern = Regex("^[A-Za-z0-9_-]+$")
        assertTrue(
            validBase64UrlPattern.matches(verifier),
            "Verifier must be Base64URL-encoded without padding (only [A-Za-z0-9_-])"
        )
    }

    @Test
    fun testGenerateCodeVerifierProducesUniqueValues() {
        val verifier1 = PkceUtils.generateCodeVerifier()
        val verifier2 = PkceUtils.generateCodeVerifier()
        assertNotEquals(verifier1, verifier2, "Subsequent verifiers should be unique")
    }

    @Test
    fun testGenerateCodeChallengeMatchesRfc7636AppendixBTestVector() {
        // Test vector from RFC 7636 Appendix B:
        // Verifier: dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
        // S256 Challenge: E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

        val challenge = PkceUtils.generateCodeChallenge(verifier)
        assertEquals(expectedChallenge, challenge)
    }

    @Test
    fun testPkceUtilAliasWorksIdentically() {
        val verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
        val challenge = PkceUtil.generateCodeChallenge(verifier)
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", challenge)
    }
}
