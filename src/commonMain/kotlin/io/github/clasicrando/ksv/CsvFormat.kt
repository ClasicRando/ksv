package io.github.clasicrando.ksv

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * CSV format for reading and writing. Some options are specific to one action so consult the doc
 * comments to know which ones are must be supplied for reading and writing.
 */
public data class CsvFormat(
    /** Delimiter character, default ',' */
    val delimiter: Char = ',',
    /** Data qualifier character, default '"' */
    val quoteChar: Char = '"',
    /** [QuoteMode], only impacts CSV writing */
    val quoteMode: QuoteMode = QuoteMode.Minimal,
    /** Character used to find comment lines. These lines are ignored while reading */
    val commentChar: Char = '#',
    /**
     * Specify headers used. When writing, these values are written to the file on the first line
     * (after [headerComments] if present). When reading, the file is assumed to contain no headers
     * and these values are used as the headers.
     */
    val headers: List<String>? = null,
    /** Supply comments to be written before the header row. Only impacts CSV writing. */
    val headerComments: List<String>? = null,
    /** Trim values when either reading or writing */
    val trimValues: Boolean = false,
    /** String values to be interpreted as null when reading or writing */
    val nullStrings: List<String>? = null,
    /**  */
    val allowTrailingDelimiter: Boolean = false,
    /** Newline characters used when writing CSV file, defaults to platform default */
    val newLine: String = PLATFORM_DEFAULT_NEWLINE,
) {
    /**
     * Initiates reading of the [source] as [CsvRow]s and allows the caller to read as they please
     * with a scoped [CsvReader]. When the [block] scope exits, the reader and its resources are
     * closed.
     */
    public inline fun <T> reader(source: Source, crossinline block: CsvReader.() -> T): T {
        check(headers?.isNotEmpty() ?: true) { "headers cannot be an empty list" }
        return InternalCsvReader(source = source, format = this).use(block)
    }

    /**
     * Wrap the supplied [sink] with a [CsvWriter] and allow the caller to write rows manually. This
     * method is only recommended for those who need advanced control over CSV writing. If you do
     * not need that, use the extension methods that supply the data to be consumed by the writer.
     * When the [block] scope exits, the writer and its resources are closed.
     */
    public inline fun writer(sink: Sink, crossinline block: CsvWriter.() -> Unit) {
        check(!headers.isNullOrEmpty()) { "Cannot write CSV with empty or null headers" }
        InternalCsvWriter(sink = sink, format = this, headers = headers).use {
            it.writeComments(headerComments)
            it.write(headers)
            block(it)    
        }
    }
}

/**
 * Return a new [CsvFormat] with [CsvFormat.headers] as each [Enum.name] values used from [E]. This
 * is a wrapper for:
 * ```
 * copy(headers = enumValues<E>().map { it.name })
 * ```
 */
public inline fun <reified E : Enum<E>> CsvFormat.withHeaderEnum(): CsvFormat {
    return copy(headers = enumValues<E>().map { it.name })
}

/**
 * Initiates a [CsvReader] using the specified [path] as a [Source].
 * ```
 * SystemFileSystem.source(path).use { reader(rawSource = it, block) }
 * ```
 *
 * @see [CsvFormat.reader]
 */
public inline fun <T> CsvFormat.reader(path: Path, crossinline block: CsvReader.() -> T): T {
    return SystemFileSystem.source(path).use { reader(rawSource = it, block) }
}

/**
 * Initiates a [CsvReader] using the specified [rawSource] as a [Source]. This is equivalent to:
 * ```
 * reader(source = rawSource.buffered(), block = block)
 * ```
 *
 * @see [CsvFormat.reader]
 */
public inline fun <T> CsvFormat.reader(
    rawSource: RawSource,
    crossinline block: CsvReader.() -> T,
): T {
    return reader(source = rawSource.buffered(), block = block)
}

/**
 * Initiates a [CsvWriter] using the specified [path] as a [Sink]. This is equivalent to:
 * ```
 * SystemFileSystem.sink(path).use { writer(rawSink = it, block = block) }
 * ```
 * 
 * @see [CsvFormat.writer]
 */
public inline fun CsvFormat.writer(path: Path, crossinline block: CsvWriter.() -> Unit) {
    SystemFileSystem.sink(path).use { writer(rawSink = it, block = block) }
}

/**
 * Initiates a [CsvWriter] using the specified [rawSink] as a [Sink]. This is equivalent to:
 * ```
 * writer(sink = rawSink.buffered(), block = block)
 * ```
 * 
 * @see [CsvFormat.writer]
 */
public inline fun CsvFormat.writer(rawSink: RawSink, crossinline block: CsvWriter.() -> Unit) {
    writer(sink = rawSink.buffered(), block = block)
}

/**
 * Initiates a [CsvWriter] using the specified [outputPath] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(outputPath: Path, rows: Iterable<List<String?>>) {
    this.write(sink = SystemFileSystem.sink(outputPath).buffered(), rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [outputPath] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(outputPath: Path, rows: Sequence<List<String?>>) {
    this.write(sink = SystemFileSystem.sink(outputPath).buffered(), rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [outputPath] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(outputPath: Path, rows: Iterator<List<String?>>) {
    this.write(sink = SystemFileSystem.sink(outputPath).buffered(), rows = rows)
}

/**
 * Initiates a [CsvWriter] using the specified [rawSink] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(rawSink: RawSink, rows: Iterable<List<String?>>) {
    this.write(sink = rawSink.buffered(), rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [rawSink] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(rawSink: RawSink, rows: Sequence<List<String?>>) {
    this.write(sink = rawSink.buffered(), rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [rawSink] as a [Sink] and writes all [rows] to the
 * [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(rawSink: RawSink, rows: Iterator<List<String?>>) {
    this.write(sink = rawSink.buffered(), rows = rows)
}

/**
 * Initiates a [CsvWriter] using the specified [sink] and writes all [rows] to the [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(sink: Sink, rows: Iterable<List<String?>>) {
    this.write(sink = sink, rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [sink] and writes all [rows] to the [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(sink: Sink, rows: Sequence<List<String?>>) {
    this.write(sink = sink, rows = rows.iterator())
}

/**
 * Initiates a [CsvWriter] using the specified [sink] and writes all [rows] to the [CsvWriter].
 *
 * @see [CsvFormat.writer]
 */
public fun CsvFormat.write(sink: Sink, rows: Iterator<List<String?>>) {
    writer(sink = sink) {
        while (rows.hasNext()) {
            write(rows.next())
        }
    }
}
