package io.github.clasicrando.ksv

/**
 * CSV stream reader used to provide a caller with a scoped value that acts like an iterator, where
 * each call to [readNextRow] returns a row until no more rows are available. At this point the
 * method then returns nulls.
 */
public interface CsvReader : Iterable<CsvRow> {
    /**
     * Request the next [CsvRow]. Returns the row or null if the stream is done reading.
     *
     * @throws CsvReadError If an error occurs while reading CSV rows. This can happen when:
     * - the number of values in a row does not match the expected header
     * - a header value is blank or null
     */
    public fun readNextRow(): CsvRow?
}
