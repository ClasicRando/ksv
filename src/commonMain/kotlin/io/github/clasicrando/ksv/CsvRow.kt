package io.github.clasicrando.ksv

/** Container for a CSV row with its [values] and a reference to the CSV's [headers] */
public data class CsvRow(
    public val headers: List<String>,
    public val values: List<String?>,
)
