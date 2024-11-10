package io.github.clasicrando.ksv

import kotlinx.io.Sink
import kotlinx.io.writeCodePointValue
import kotlinx.io.writeString

/** Internal [CsvWriter] implementation */
@PublishedApi
internal class InternalCsvWriter(
    private val sink: Sink,
    private val format: CsvFormat,
    private val headers: List<String>,
) : CsvWriter, AutoCloseable {
    /** Write each header comment (if any) prefixed by the [CsvFormat.commentChar] */
    @PublishedApi
    internal fun writeComments(headerComments: List<String>?) {
        if (headerComments != null) {
            for (comment in headerComments) {
                sink.writeCodePointValue(format.commentChar.code)
                sink.writeString(comment)
                sink.writeString(PLATFORM_DEFAULT_NEWLINE)
            }
        }
    }

    override fun write(vararg values: String?) {
        checkAgainstHeader(values.size)
        for (i in values.indices) {
            writeValue(values[i])
            if (i < values.size - 1) {
                sink.writeCodePointValue(format.delimiter.code)
            }
        }
        sink.writeString(PLATFORM_DEFAULT_NEWLINE)
    }

    override fun write(values: List<String?>) {
        checkAgainstHeader(values.size)
        for (i in values.indices) {
            writeValue(values[i])
            if (i < values.size - 1) {
                sink.writeCodePointValue(format.delimiter.code)
            }
        }
        sink.writeString(PLATFORM_DEFAULT_NEWLINE)
    }

    private fun checkAgainstHeader(rowSize: Int) {
        if (rowSize != headers.size) {
            throw CsvWriteError(
                "Supplied number of row values does not match header size. " +
                        "Expected ${headers.size}, found $rowSize"
            )
        }
    }

    private fun writeValue(value: String?) {
        var data: String? = value
        if (format.trimValues && data != null) {
            data = data.trim()
        }
        format.nullStrings?.let {
            if (it.contains(data)) {
                data = null
            }
        }
        data.takeIf { !it.isNullOrEmpty() }?.let {
            val needsQuoting = format.quoteMode == QuoteMode.All || it.requiresQuoting()
            if (needsQuoting) {
                sink.writeCodePointValue(format.quoteChar.code)
            }
            for (c in it) {
                if (needsQuoting && c == format.quoteChar) {
                    sink.writeCodePointValue(format.quoteChar.code)
                }
                sink.writeCodePointValue(c.code)
            }
            if (needsQuoting) {
                sink.writeCodePointValue(format.quoteChar.code)
            }
        }
    }

    private fun String.requiresQuoting(): Boolean {
        return this.any { c ->
            c == format.quoteChar || c == format.delimiter || c.code == LF || c.code == CR
        }
    }

    override fun close() {
        sink.close()
    }
}
