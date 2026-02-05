import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import no.nav.tms.common.postgres.Postgres
import no.nav.tms.common.postgres.PostgresDatabase
import org.flywaydb.core.Flyway
import org.testcontainers.postgresql.PostgreSQLContainer

object LocalPostgresDatabase {

    private val container = PostgreSQLContainer("postgres:14.5")
        .also { it.start() }

    val database: PostgresDatabase by lazy {
        Postgres.connectToContainer(container).also {
            migrate(it.dataSource)
        }
    }

    fun getCleanInstance(): PostgresDatabase {
        resetInstance()
        return database
    }

    fun resetInstance() {
        database.cleanTables(
            "beredskapsvarsel",
            "varsel",
            "microfrontends",
            "utkast",
            "innlogging_per_dag"
        )
    }

    private fun migrate(dataSource: HikariDataSource) {
        Flyway.configure()
            .connectRetries(3)
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}

fun PostgresDatabase.cleanTables(vararg tables: String) {
    tables.forEach { table ->
        update {
            queryOf("delete from $table")
        }
    }
}

inline fun <T> T.assert(block: T.() -> Unit): T =
    apply {
        block()
    }
