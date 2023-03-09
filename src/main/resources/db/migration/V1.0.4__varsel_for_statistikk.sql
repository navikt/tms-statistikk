create table varsel(
    eventId text primary key unique,
    ident text,
    type text,
    namespace text,
    appnavn text,
    tekstlengde int,
    lenke bool,
    sikkerhetsnivaa int,
    aktiv bool,
    frist bool,
    forstBehandlet timestamp,
    inaktivertTidspunkt timestamp,
    inaktivertKilde text,
    eksternVarslingBestilt bool,
    eksternVarslingSendtSms bool,
    eksternVarslingSendtEpost bool
);
