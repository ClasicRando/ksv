package io.github.clasicrando.ksv

/**
 * Value quoting mode. This only impacts writing and for simplicity there is only the option to
 * quote all values or quote the minimal values.
 */
public enum class QuoteMode {
    /** Quote every value no mater the content */
    All,
    /** Only quote values that contains a quote char, delimiter or newline character */
    Minimal,
}
