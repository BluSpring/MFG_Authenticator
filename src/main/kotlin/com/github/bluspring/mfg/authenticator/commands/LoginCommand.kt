package com.github.bluspring.mfg.authenticator.commands

import com.github.bluspring.mfg.authenticator.database.DatabaseManager
import com.github.bluspring.mfg.authenticator.listeners.PlayerOnLoginListener
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object LoginCommand : CommandExecutor {
    private val attempts = mutableMapOf<Player, Int>()
    private const val MAX_TRIES = 5

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as Player

        if (PlayerOnLoginListener.loggedIn.contains(player)) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You're already logged in, mate")
            return true
        }

        if (DatabaseManager.database.players.find {it.uuid == player.uniqueId.toString()} == null) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You don't have your account registered! Please go register now.\n/register <password> <confirm-password>")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You didn't specify any arguments!")
            return false
        }

        val result = DatabaseManager.login(player, args[0])

        if (!result) {
            if (attempts[player] != null) {
                attempts[player] = attempts[player]!!.plus(1)
            } else {
                attempts[player] = 1
            }

            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Incorrect password!")

            if (attempts[player]!! >= MAX_TRIES) {
                sender.kickPlayer("Failed to login!")
                attempts.remove(player)
            }

            return false
        } else {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> Successfully logged you in! Welcome aboard!")

            PlayerOnLoginListener.loggedIn.add(sender)
            attempts.remove(player)
            DatabaseManager.database.players.find { it.uuid == sender.player!!.uniqueId.toString() }!!.lastIp = sender.player!!.address.address.hostAddress

            DatabaseManager.writeFile()
        }

        return true
    }
}