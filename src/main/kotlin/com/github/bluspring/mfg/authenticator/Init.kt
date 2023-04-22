package com.github.bluspring.mfg.authenticator

import com.github.bluspring.mfg.authenticator.commands.LoginCommand
import com.github.bluspring.mfg.authenticator.commands.RegisterCommand
import com.github.bluspring.mfg.authenticator.commands.ResetPasswordCommand
import com.github.bluspring.mfg.authenticator.listeners.PlayerOnLoginListener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Init : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        logger.info("MFG-Authenticator has been enabled!")

        if (!plugin.dataFolder.exists())
            plugin.dataFolder.mkdir()

        if (!File(plugin.dataFolder, "config.yml").exists() || File(plugin.dataFolder, "config.yml").readText() == "")
            this.saveDefaultConfig()

        plugin.server.pluginManager.registerEvents(PlayerOnLoginListener, this)

        this.getCommand("register")?.setExecutor(RegisterCommand)
        this.getCommand("login")?.setExecutor(LoginCommand)
        this.getCommand("reset-password")?.setExecutor(ResetPasswordCommand)
    }

    override fun onDisable() {
        logger.info("MFG-Authenticator has been disabled.")
    }

    companion object {
        lateinit var plugin: JavaPlugin
    }
}