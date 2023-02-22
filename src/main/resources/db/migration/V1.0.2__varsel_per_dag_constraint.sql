ALTER TABLE varsler_per_dag ADD CONSTRAINT varsler_per_dag_varsel_type UNIQUE (dato, ident, type, ekstern_varsling);
