package no.nav.tms.statistikk.microfrontends

import kotliquery.queryOf
import no.nav.tms.common.postgres.PostgresDatabase
import java.time.LocalDateTime
import java.time.ZoneId

class MicrofrontendRepository(val database: PostgresDatabase) {
    fun insertMicrofrontend(action: String, microfrontendId: String, ident: String, initiatedBy: String?) {

        val newEvent = database.singleOrNull {
            queryOf(
                """select action from microfrontends 
                 where microfrontend_id=:microfrontendId and ident=:ident
                 order by initiated_time desc
                 LIMIT 1
                """.trimIndent(), mapOf(
                    "microfrontendId" to microfrontendId,
                    "ident" to ident
                )
            ).map { row ->
                row.string("action") != action
            }
        } ?: (action == "enable")

        if (newEvent) {
            database.update {
                queryOf(
                    "INSERT INTO microfrontends(ident,action,initiated_time, microfrontend_id,initiated_by) " +
                            "VALUES(:ident,:action,:initiatedTime,:microfrontendId,:initiatedBy)" ,
                    mapOf(
                        "ident" to ident,
                        "action" to action,
                        "initiatedTime" to LocalDateTime.now(ZoneId.of("UTC")),
                        "microfrontendId" to microfrontendId,
                        "initiatedBy" to initiatedBy
                    )
                )
            }
        }
    }
}
