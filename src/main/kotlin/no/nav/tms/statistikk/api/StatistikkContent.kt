package no.nav.tms.statistikk.api

import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import java.io.OutputStream
import java.time.LocalDate


fun HTML.buildStats() {
    head {
        title("Min side stats")
    }
    body {
        h1("Backend statistikk for min side for ${LocalDate.now()}")

        a {
            href = "/hent/lastned/utkast"
            text("Last ned CSV-fil")
        }

    }
}

fun OutputStream.writeInnloggingCSV(innlogingPerDag: Int) =
    bufferedWriter().apply {
        write("""Innlogging etter ekstern varsling,$innlogingPerDag""")
        flush()
    }

class StatistikkContentException(message: String) : Exception(message)
