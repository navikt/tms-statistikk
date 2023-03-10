CREATE TABLE IF NOT EXISTS innlogging_etter_eksternt_varsel
(
    eventId TEXT,
    dato TEXT,
    ident TEXT NOT NULL,
    sendtTimestamp TIMESTAMP NOT NULL,
    innloggetTimestamp TIMESTAMP DEFAULT NULL,
    epost BOOLEAN,
    sms BOOLEAN,
    PRIMARY KEY (eventId,dato)
);
