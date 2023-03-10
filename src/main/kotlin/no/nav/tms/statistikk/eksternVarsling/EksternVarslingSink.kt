package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.RapidsConnection
import java.lang.IllegalArgumentException

const val midlertidigIdent = "987654"

class EksternVarslingSink(
    rapidsConnection: RapidsConnection,
    eksternVarslingRepository: EksternVarslingRepository
) {


}

enum class Kanal {
    SMS, EPOST;
}
