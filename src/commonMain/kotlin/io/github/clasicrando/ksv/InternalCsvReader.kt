package io.github.clasicrando.ksv

import kotlinx.io.Source

/**
 * Internal [CsvReader] implementation. Creates a [CsvLexer] and initiates a [Sequence] yielding
 * [CsvRow]s from the lexer. The [Sequence] acts as the iterator that backs the reader.
 */
@PublishedApi
internal class InternalCsvReader(source: Source, format: CsvFormat) : CsvReader, AutoCloseable {
    private val lexer = CsvLexer(source = source, format = format)
    private val rows: Sequence<CsvRow> = sequence {
        val iter = lexer.tokens().iterator()
        var headers = lexer.format.headers
        val rowItems = mutableListOf<String?>()
        var rowNumber = 1
        while (iter.hasNext()) {
            when (val token = iter.next()) {
                is Token.Comment -> continue
                Token.EndOfFile -> break
                Token.EndOfRecord -> {
                    if (headers != null) {
                        if (headers.size != rowItems.size) {
                            throw CsvReadError(
                                "Mismatch in header size for row #$rowNumber. " +
                                    "Expected ${headers.size}, found ${rowItems.size}"
                            )
                        }
                        yield(CsvRow(headers, rowItems.toList()))
                        rowNumber++
                    } else {
                        if (rowItems.any { it.isNullOrBlank() }) {
                            throw CsvReadError("Found header value that is null or blank")
                        }
                        headers = rowItems.map { it!! }
                    }
                    rowItems.clear()
                }
                is Token.Value -> {
                    var value: String? = token.data
                    if (lexer.format.trimValues && value != null) {
                        value = value.trim()
                    }
                    lexer.format.nullStrings?.let {
                        if (it.contains(value)) {
                            value = null
                        }
                    }

                    rowItems.add(value)
                }
            }
        }

        if (iter.hasNext()) {
            throw CsvReadError("Exited parsing before all token where processed")
        }
    }
    private val iter = rows.iterator()

    override fun iterator(): Iterator<CsvRow> {
        return iter
    }

    override fun readNextRow(): CsvRow? {
        if (iter.hasNext()) {
            return iter.next()
        }
        return null
    }

    override fun close() {
        lexer.close()
    }
}
