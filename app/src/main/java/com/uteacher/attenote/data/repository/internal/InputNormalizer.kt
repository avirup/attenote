package com.uteacher.attenote.data.repository.internal

object InputNormalizer {

    private val whitespaceRegex = Regex("\\s+")

    /**
     * Trims edge whitespace and collapses internal whitespace to a single space.
     */
    fun normalize(input: String): String {
        return input.trim().replace(whitespaceRegex, " ")
    }

    /**
     * Compare two values with normalization and case-insensitive matching.
     */
    fun areEqual(a: String, b: String): Boolean {
        return normalize(a).equals(normalize(b), ignoreCase = true)
    }
}
