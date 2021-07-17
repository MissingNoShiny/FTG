package xyz.missingnoshiny.ftg.server

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DatabaseInitializer {
    init {
        Database.connect(getDataSource())
    }

    private fun getDataSource(): DataSource {
        val appConfig = HoconApplicationConfig(ConfigFactory.load())
        val dbConfig = HikariConfig().apply {
            jdbcUrl = appConfig.property("db.url").getString()
            username = appConfig.property("db.username").getString()
            password = appConfig.property("db.password").getString()
        }
        dbConfig.validate()
        return HikariDataSource(dbConfig)
    }
}