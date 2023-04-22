package com.github.bluspring.mfg.authenticator.commands

import com.github.bluspring.mfg.authenticator.Init
import com.github.bluspring.mfg.authenticator.database.DatabaseManager
import com.github.bluspring.mfg.authenticator.database.EncryptionManager
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ResetPasswordCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You are not console!")
            return false
        }

        if (args.isNullOrEmpty() || args.size == 1) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You didn't specify any arguments!")
            return false
        }

        if (DatabaseManager.database.players.find {it.username == args[0]} == null) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Couldn't find that player!")
            return false
        }

        if (args[1].length < 10) {
            sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Password too short!")
            return false
        }

        val playerDb = DatabaseManager.database.players.find {it.username == args[0]}!!
        playerDb.encryptedPassword = EncryptionManager.encrypt(args[1].toByteArray(), args[1])

        DatabaseManager.writeFile()

        sender.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Successfully wrote new password!")

        return true
    }
}