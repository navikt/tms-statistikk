package no.nav.tms.statistikk.microfrontends

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import no.nav.tms.statistikk.database.LocalDateTimeHelper

class MicrofrontendRepository(val database: Database) {
    fun insertMicrofrontend(action: String, microfrontendId: String, ident: String) {
        database.update {
            queryOf(
                "INSERT INTO microfrontends(ident,time,action, microfrontend_id) " +
                        "VALUES(:ident,:action,:time,:microfrontendId)",
                mapOf(
                    "ident" to ident,
                    "action" to action,
                    "time" to LocalDateTimeHelper.nowAtUtc(),
                    "microfrontendId" to microfrontendId
                )
            )
        }
    }
}

/*
id serial primary key,
time timestamp not null,
ident varchar(11) not null,
action varchar(15) not null,
microfrontend_id varchar(50) not null */