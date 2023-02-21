CREATE TABLE IF NOT EXISTS varsler_per_dag
(
    dato DATE NOT NULL,
    ident TEXT NOT NULL,
    type TEXT NOT NULL,
    ekstern_varsling BOOL NOT NULL,
    antall INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS innlogging_per_dag
(
    dato DATE NOT NULL,
    ident TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS varsler_per_dag_dato ON varsler_per_dag(dato);
CREATE INDEX IF NOT EXISTS varsler_per_dag_ident ON varsler_per_dag(ident);

ALTER TABLE innlogging_per_dag ADD CONSTRAINT innlogging_dato_ident UNIQUE (dato, ident);
