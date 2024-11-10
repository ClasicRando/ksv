package io.github.clasicrando.ksv

/** Known tokens that [CsvLexer] can yield */
internal sealed interface Token {
    /** Token as a CSV field value and it's [String] [data] */
    data class Value(val data: String) : Token
    /** Token indicating that the current record has completed */
    data object EndOfRecord : Token
    /** Token indicating the stream of data has been completed */
    data object EndOfFile : Token
    /** Token as a CSV comment line contents */
    data class Comment(val inner: String) : Token
}
