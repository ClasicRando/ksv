package io.github.clasicrando.ksv

internal const val CR: Int = '\r'.code
internal const val LF: Int = '\n'.code
internal const val EOF: Int = -1
internal const val SOF: Int = -2

/**
 * Platform dependent newline character. For JVM this defers to the system property for newlines.
 * - Windows -> "\r\n"
 * - Linux -> "\n"
 * - macOS -> "\r"
 */
@PublishedApi
internal expect val PLATFORM_DEFAULT_NEWLINE: String
