package com.github.bluspring.mfg.authenticator.config

import com.charleskorn.kaml.Yaml
import com.github.bluspring.mfg.authenticator.Init
import java.io.File

object Config {
    val config: IConfig = Yaml.default.decodeFromString(IConfig.serializer(), File(Init.plugin.dataFolder, "config.yml").readText())

    fun saveConfig() {
        val text = Yaml.default.encodeToString(IConfig.serializer(), config)
        File(Init.plugin.dataFolder, "config.yml").writeText(text)
    }
}