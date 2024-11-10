package io.github.clasicrando.ksv

/**
 * CSV stream writer used to provide a caller with a scoped value for writing rows 1 at a time.
 * Values can be written as a `vararg` or as a [List] but each set of values must match the number
 * of headers for the CSV.
 */
public interface CsvWriter {
    /**
     * Write all values to the CSV stream.
     *
     * @throws CsvWriteError if the number of values supplied does not match the header
     */
    public fun write(vararg values: String?)

    /**
     * Write all values to the CSV stream.
     *
     * @throws CsvWriteError if the number of values supplied does not match the header
     */
    public fun write(values: List<String?>)
}
