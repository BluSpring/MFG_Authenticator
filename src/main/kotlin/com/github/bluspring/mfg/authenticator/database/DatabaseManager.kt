package com.github.bluspring.mfg.authenticator.database

import com.charleskorn.kaml.Yaml
import com.github.bluspring.mfg.authenticator.Init
import com.github.bluspring.mfg.authenticator.config.Config
import org.bukkit.entity.Player
import java.io.File

object DatabaseManager {
    val database: YAMLDatabaseData = if (true) {
        val dbFile = File(Init.plugin.dataFolder, "database.yml")
        if (!dbFile.exists() || dbFile.readText().isBlank()) {
            dbFile.createNewFile()

            YAMLDatabaseData()
        } else
            Yaml.default.decodeFromString(YAMLDatabaseData.serializer(), dbFile.readText())
    } else {
        throw Exception("At the moment, only YAML files are supported for databases.")
    }

    fun writeFile() {
        if (true) {
            File(Init.plugin.dataFolder, "database.yml").writeText(Yaml.default.encodeToString(YAMLDatabaseData.serializer(), database))
        }
    }

    fun login(player: Player, password: String): Boolean {
        if (database.players.find { it.uuid == player.uniqueId.toString() } == null) {
            player.kickPlayer("You're not registered!")
            return false
        }

        val playerDb = database.players.find {it.uuid == player.uniqueId.toString()}!!

        return try {
            EncryptionManager.decrypt(playerDb.encryptedPassword, password) == password
        } catch (_: Exception) {
            false
        }
    }

    fun register(player: Player, password: String): Boolean {
        if (database.players.find {it.uuid == player.uniqueId.toString()} != null) {
            player.kickPlayer("An account for you already exists!")
            return false
        }

        val playerDb = DatabasePlayers(
            username = player.name,
            uuid = player.uniqueId.toString(),
            encryptedPassword = EncryptionManager.encrypt(password.toByteArray(), password),
            lastIp = player.address.address.hostAddress
        )
        database.players.add(playerDb)
        writeFile()
        return true
    }
}