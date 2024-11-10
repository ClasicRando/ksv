package io.github.clasicrando.ksv

import kotlinx.io.Source
import kotlinx.io.readCodePointValue
import kotlinx.io.readLine

/**
 * Lexer for CSV data. Yields a [Sequence] of [Token] values to indicate what values it has read
 * from the CSV. This does not validate the format of the CSV but rather just returns tokens it
 * finds.
 */
@PublishedApi
internal class CsvLexer(internal val source: Source, internal val format: CsvFormat) :
    AutoCloseable {
    private var endReached = false

    /**
     * Read the [source] yielding a stream of tokens as the CSV contents. The stream ends once it
     * reads an EOF char or [Source.exhausted] is true.
     */
    fun tokens(): Sequence<Token> = sequence {
        var lastChar = SOF
        var currentChar: Int
        val builder = StringBuilder()
        while (!endReached) {
            currentChar = source.readChar()
            if (currentChar == EOF) {
                endReached = true
                continue
            }
            if (isCommentStart(currentChar)) {
                val comment = source.readLine()?.trim()
                if (comment == null) {
                    endReached = true
                    yield(Token.EndOfFile)
                    lastChar = currentChar
                    continue
                }
                yield(Token.Comment(comment))
                lastChar = currentChar
                continue
            }
            builder.clear()
            var inQuote = false
            while (true) {
                when (val chr = currentChar.toChar()) {
                    EOF.toChar() -> {
                        if (builder.isNotEmpty()) {
                            yield(Token.Value(builder.toString()))
                            yield(Token.EndOfRecord)
                        }
                        endReached = true
                        break
                    }
                    format.delimiter -> {
                        if (inQuote) {
                            builder.append(chr)
                        } else {
                            yield(Token.Value(builder.toString()))
                            builder.clear()
                        }
                    }
                    format.quoteChar -> {
                        if (lastChar == format.quoteChar.code) {
                            builder.append(format.quoteChar)
                            inQuote = true
                        } else {
                            inQuote = !inQuote
                        }
                    }
                    CR.toChar(),
                    LF.toChar() -> {
                        if (inQuote) {
                            builder.append(chr)
                        } else {
                            if (chr.code == CR && source.peekChar() == LF) {
                                source.readChar()
                            }
                            yield(Token.Value(builder.toString()))
                            yield(Token.EndOfRecord)
                            break
                        }
                    }
                    else -> {
                        builder.append(chr)
                    }
                }
                lastChar = currentChar
                currentChar = source.readChar()
            }
        }
    }

    private fun isCommentStart(char: Int): Boolean {
        return format.commentChar.code == char
    }

    private fun Source.readChar(): Int {
        if (this.exhausted()) {
            return EOF
        }
        return this.readCodePointValue()
    }

    private fun Source.peekChar(): Int {
        return this.peek().readChar()
    }

    override fun close() {
        source.close()
    }
}
