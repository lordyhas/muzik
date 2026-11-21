package com.lordyhas.muzik.service

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

/**
 * The player as the media session sees it: Next stays available on the last song, so the
 * notification and the system media controls never drop the button (Samsung then moves the
 * shuffle button into its place). On the last song, Next goes back to the first one, paused.
 */
@UnstableApi
class NextAlwaysPlayer(player: Player) : ForwardingPlayer(player) {

    private val listeners = mutableMapOf<Player.Listener, Player.Listener>()

    override fun getAvailableCommands(): Player.Commands = withNext(super.getAvailableCommands())

    override fun isCommandAvailable(command: Int): Boolean =
        super.isCommandAvailable(command) || (command in NEXT_COMMANDS && hasQueue())

    override fun seekToNext() {
        if (wrappedPlayer.hasNextMediaItem()) super.seekToNext() else backToStart()
    }

    override fun seekToNextMediaItem() {
        if (wrappedPlayer.hasNextMediaItem()) super.seekToNextMediaItem() else backToStart()
    }

    // The session hears about command changes through its listener, with the player's own list:
    // hand it ours instead, or Media3 controllers would still lose Next
    override fun addListener(listener: Player.Listener) {
        super.addListener(listeners.getOrPut(listener) { withNextCommands(listener) })
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)?.let { super.removeListener(it) }
    }

    private fun hasQueue() = !wrappedPlayer.currentTimeline.isEmpty

    private fun withNext(commands: Player.Commands): Player.Commands =
        if (!hasQueue()) commands else commands.buildUpon().addAll(*NEXT_COMMANDS.toIntArray()).build()

    private fun backToStart() {
        if (!hasQueue()) return
        wrappedPlayer.seekToDefaultPosition(0)
        wrappedPlayer.pause()
    }

    /**
     * Passes every call on to [listener], with Next added to the available commands. A proxy
     * rather than Kotlin's `by`, which skips the interface's Java default methods (all of them).
     */
    private fun withNextCommands(listener: Player.Listener): Player.Listener =
        Proxy.newProxyInstance(listener.javaClass.classLoader, arrayOf(Player.Listener::class.java)) { _, method, args ->
            val callArgs = args ?: emptyArray()
            if (method.name == "onAvailableCommandsChanged") {
                callArgs[0] = withNext(callArgs[0] as Player.Commands)
            }
            try {
                method.invoke(listener, *callArgs)
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        } as Player.Listener

    private companion object {
        val NEXT_COMMANDS = listOf(Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
    }
}
