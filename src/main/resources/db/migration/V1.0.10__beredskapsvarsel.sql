create table beredskapsvarsel
(
    id             serial primary key,
    beredskap_tittel varchar not null,
    varselId       varchar not null references varsel (eventid)
)

