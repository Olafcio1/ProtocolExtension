package pl.olafcio.protocolextension.server.api.virtual;

import org.bukkit.entity.Player;

public interface ProtocolExtensionListener {
    default void onMouseMove(Player player, double x, double y) {}
    default void onKeyPressed(Player player, int key) {}

    //TODO:[onConnect and onDisconnect](Player player) methods
}
