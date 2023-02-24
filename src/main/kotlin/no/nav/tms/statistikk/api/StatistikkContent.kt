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

class Statistikk(val innlogginger_per_dag: Int, val måned: String, val år: String) {
    val innlogginger_per_dag_line = """${måned} $år,$innlogginger_per_dag"""

    companion object {
        fun HTML.buildStats(statistikk: Statistikk?) {
            require(statistikk != null)

            head {
                title("Min side stats")
            }
            body {
                h1("Backend statistikk for min side, ${statistikk.måned} ${statistikk.måned}")
                table {
                    tr {
                        th {
                            +"Gjennomsnittlig innlogging på min side pr dag"
                        }
                        td {
                            +"${statistikk.innlogginger_per_dag}"
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
    writer.write("""Måned,Gjennomsnitt innloggede pr dag""")
    writer.newLine()
    writer.write(statistikk.innlogginger_per_dag_line)
    writer.flush()
}