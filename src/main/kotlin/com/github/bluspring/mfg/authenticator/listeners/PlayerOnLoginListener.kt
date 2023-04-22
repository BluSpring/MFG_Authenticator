package com.github.bluspring.mfg.authenticator.listeners

import com.destroystokyo.paper.Title
import com.github.bluspring.mfg.authenticator.Init
import com.github.bluspring.mfg.authenticator.database.DatabaseManager
import com.google.gson.JsonParser
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

//fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

object PlayerOnLoginListener : Listener {
    /*const val SESSION_SERVER = "sessionserver.mojang.com"
    const val MOJANG_API = "api.mojang.com"*/
    private val VALID_CHARS: Pattern = Pattern.compile("[a-zA-Z0-9_*]*")

    // Mutable maps of UUID and player
    val loggedIn = mutableListOf<Player>()

    init {
        Init.plugin.server.scheduler.runTaskTimer(Init.plugin, Runnable {
            loggedIn.forEach {
                if (!Init.plugin.server.onlinePlayers.contains(it)) {
                    loggedIn.remove(it)
                }
            }
        }, 7200L, 7200L)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
        if (e.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_BANNED || e.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL || e.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST)
            return

        if (!isValidName(e.name)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your username is invalid!")
            return
        }

        e.allow()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerLogin(e: PlayerLoginEvent) {
        e.allow()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (e.isCancelled) return

        if (!loggedIn.contains(e.entity)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamaged(e: EntityDamageEvent) {
        if (e.isCancelled) return
        if (e.entity !is Player) return

        if (!loggedIn.contains(e.entity as Player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.isCancelled) return

        if (!loggedIn.contains(e.player)) {
            e.isCancelled = true
            e.player.sendTitle(Title("", "${ChatColor.RED}You're not authenticated!"))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (DatabaseManager.database.players.find { it.uuid == e.player.uniqueId.toString() } == null)
            e.player.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Please register now!\n${ChatColor.YELLOW}/register <password> <confirmPassword>")
        else {
            if (DatabaseManager.database.players.find { it.uuid == e.player.uniqueId.toString() }!!.lastIp == e.player.address.address.hostAddress) {
                loggedIn.add(e.player)
                return
            }

            e.player.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}Please login now!\n${ChatColor.YELLOW}/login <password>")
        }

        Init.plugin.server.scheduler.runTaskLater(Init.plugin, Runnable {
            if (!loggedIn.contains(e.player))
                e.player.kickPlayer("${ChatColor.RED}You didn't authenticate in time!")
        }, 2400)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerLeave(e: PlayerQuitEvent) {
        if (loggedIn.contains(e.player))
            loggedIn.remove(e.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerKick(e: PlayerKickEvent) {
        if (loggedIn.contains(e.player))
            loggedIn.remove(e.player)
    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        if (!loggedIn.contains(e.player)) {
            e.isCancelled = true
            e.player.sendTitle(Title("", "${ChatColor.RED}You're not authenticated!"))
        }
    }

    @EventHandler
    fun onPlayerChat(e: AsyncPlayerChatEvent) {
        if (e.message.startsWith("/")) return

        if (!loggedIn.contains(e.player)) {
            e.isCancelled = true
            e.player.sendMessage("${ChatColor.RED}Authenticator ${ChatColor.GREEN}>> ${ChatColor.RED}You've not authenticated yet!")
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (!loggedIn.contains(e.player))
            e.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteractEntity(e: PlayerInteractEntityEvent) {
        if (!loggedIn.contains(e.player))
            e.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteractAtEntity(e: PlayerInteractAtEntityEvent) {
        if (!loggedIn.contains(e.player))
            e.isCancelled = true
    }

    @EventHandler
    fun onPlayerAttemptPickup(e: PlayerAttemptPickupItemEvent) {
        if (!loggedIn.contains(e.player))
            e.isCancelled = true
    }

    @EventHandler
    fun onAttackEntity(e: EntityDamageByEntityEvent) {
        if (e.damager is Player && !loggedIn.contains((e.damager as Player).player))
            e.isCancelled = true
    }

    private fun isValidName(name: String): Boolean {
        if (name.length > 16 || name.length < 3)
            return false

        /*if (!VALID_CHARS.matcher(name).matches())
            return false*/

        return true
    }
}