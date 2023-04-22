package com.github.bluspring.mfg.authenticator.commands

import com.github.bluspring.mfg.authenticator.database.DatabaseManager
import com.github.bluspring.mfg.authenticator.listeners.PlayerOnLoginListener
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RegisterCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as Player

        if (PlayerOnLoginListener.loggedIn.contains(player)) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You're already logged in, mate")
            return true
        }

        if (DatabaseManager.database.players.find {it.uuid == player.uniqueId.toString()} != null) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You're already registered!")
            return true
        }

        if (args.isNullOrEmpty() || args.size == 1) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You didn't specify any arguments!")
            return false
        }

        if (args[0].length < 8) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Password too short!")
            return false
        }

        if (args[0] != args[1]) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}The passwords don't match!")
            return false
        }

        val result = DatabaseManager.register(player, args[0])

        if (!result) {
            sender.kickPlayer("Failed to register! (please contact an admin immediately!)")
            return false
        } else {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Successfully registered! Welcome aboard!")
            PlayerOnLoginListener.loggedIn.add(sender)
        }

        DatabaseManager.writeFile()

        return true
    }
}