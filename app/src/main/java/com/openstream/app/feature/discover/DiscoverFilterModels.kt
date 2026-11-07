package com.openstream.app.feature.discover

enum class EraFilter(val predicate: (Int) -> Boolean) {
    ALL({ true }),
    SILENT({ it <= 1929 }),
    THIRTIES({ it in 1930..1939 }),
    FORTIES({ it in 1940..1949 }),
    FIFTIES({ it in 1950..1959 }),
    SIXTIES_PLUS({ it >= 1960 })
}

enum class RuntimeFilter(val predicate: (Int) -> Boolean) {
    ALL({ true }),
    SHORT({ it > 0 && it < 60 }),
    MEDIUM({ it in 60..90 }),
    LONG({ it in 91..120 }),
    EPIC({ it > 120 })
}

enum class SortOrder {
    YEAR_DESC,
    YEAR_ASC,
    TITLE_ASC,
    TITLE_DESC,
    RUNTIME_ASC,
    RUNTIME_DESC
}
