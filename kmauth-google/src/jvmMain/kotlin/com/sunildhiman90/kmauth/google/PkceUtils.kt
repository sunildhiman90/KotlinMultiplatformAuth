package com.sunildhiman90.kmauth.google

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Utility for Proof Key for Code Exchange (PKCE) as defined in RFC 7636.
 * Used to secure OAuth 2.0 authorization code grant flows on Desktop (JVM).
 */
object PkceUtils {

    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure random code verifier string
     * between 43 and 128 characters using Base64URL encoding without padding.
     *
     * @param byteLength Number of random bytes to generate (default is 64, yielding 86 URL-safe characters).
     * @return Base64URL-encoded unpadded code verifier string.
     */
    fun generateCodeVerifier(byteLength: Int = 64): String {
        require(byteLength in 32..96) {
            "byteLength must be between 32 and 96 bytes to produce a verifier between 43 and 128 characters"
        }
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Generates a code challenge from the given code verifier by hashing
     * it with SHA-256 and Base64URL encoding the result without padding.
     *
     * @param verifier The code verifier string.
     * @return Base64URL-encoded unpadded SHA-256 hash (code challenge).
     */
    fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}

/**
 * Alias for [PkceUtils].
 */
typealias PkceUtil = PkceUtils
