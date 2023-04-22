package com.github.bluspring.mfg.authenticator.database

import kotlinx.serialization.Serializable

@Serializable
data class YAMLDatabaseData(
    val players: MutableList<DatabasePlayers> = mutableListOf()
)

@Serializable
data class DatabasePlayers(
    val username: String,
    val uuid: String,
    var encryptedPassword: String,
    var lastIp: String
)