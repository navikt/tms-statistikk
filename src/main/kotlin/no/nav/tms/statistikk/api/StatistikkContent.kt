package no.nav.tms.statistikk.api

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.title
import kotlinx.html.tr
import java.io.OutputStream
import java.time.LocalDate

class Statistikk(val innloggingerPerDag: Int) {
    companion object {
        fun HTML.buildStats(statistikk: Statistikk?) {
            require(statistikk != null)

            head {
                title("Min side stats")
            }
            body {
                h1("Backend statistikk for min side for ${LocalDate.now()}")
                table {
                    tr {
                        th {
                            +"Gjennomsnittlig innlogging på min side pr dag"
                        }
                        td {
                            +"${statistikk.innloggingerPerDag}"
                        }
                    }
                }
                a {
                    href = "/hent/lastned"
                    text("Last ned CSV-fil")
                }

            }
        }
    }
}

internal fun OutputStream.writeCsv(statistikk: Statistikk?) {
    require(statistikk != null)
    val writer = bufferedWriter()
    writer.write("""Gjennomsnitt innloggede pr dag, ${statistikk.innloggingerPerDag}""")
    writer.flush()
}

class StatistikkContentException(message: String): Exception(message)