package pl.olafcio.protocolextension.server.api.virtual;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface API {
    //#region Listeners
    void registerListener(ProtocolExtensionListener listener);
    boolean unregisterListener(ProtocolExtensionListener listener);

    //#region Listener events
    void dispatchEvent(String method, Player player, Class<?>[] types, Object[] values);

    //#region Player methods
    void forceHUD(Player player, boolean state);
    void putHUD(Player player, short id, double x, double y, Component text);
    void deleteHUD(Player player, short id);
}
