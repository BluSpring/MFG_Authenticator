package com.github.bluspring.mfg.authenticator.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IConfig(
    @SerialName("config-version")
    val configVersion: String,

    @SerialName("authentication-type")
    val authenticationType: String,

    @SerialName("remember-ip")
    val rememberIp: Boolean,

    @SerialName("remember-ip-timeout")
    val rememberIpTimeout: Long,

    val database: DatabaseMainConfig
)

@Serializable
data class DatabaseMainConfig(
    val default: DatabaseConfig,
    val fallback: List<DatabaseConfig>
)

@Serializable
data class DatabaseConfig(
    val type: String,
    val file: String = "",
    @SerialName("connection-uri")
    val connectionUri: String = ""
)