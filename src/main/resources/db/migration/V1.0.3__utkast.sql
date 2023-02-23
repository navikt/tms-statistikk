CREATE TABLE IF NOT EXISTS utkast
(
    ident TEXT NOT NULL,
    utkast_id TEXT NOT NULL UNIQUE,
    dato DATE NOT NULL,
    event TEXT NOT NULL,
    antall_språk INT NOT NULL
);
