CREATE TABLE IF NOT EXISTS innlogging_etter_eksternt_varsel
(
    eventId TEXT NOT NULL PRIMARY KEY,
    ident TEXT NOT NULL,
    sendtTimestamp TIMESTAMP NOT NULL,
    innloggetTimestamp TIMESTAMP DEFAULT NULL
);
