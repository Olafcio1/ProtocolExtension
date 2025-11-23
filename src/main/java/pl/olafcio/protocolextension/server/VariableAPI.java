package pl.olafcio.protocolextension.server;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionListener;

import java.util.HashMap;

public final class VariableAPI implements ProtocolExtensionListener {
    private static final HashMap<Player, Position> plr2mousePos = new HashMap<>();
    //TODO: plr2mousePos.remove(player) in an onDisconnect method in a future release

    @Override
    public void onMouseMove(Player player, double x, double y) {
        plr2mousePos.put(player, new Position(x, y));
    }

    public static @Nullable Position getMousePos(Player player) {
        return plr2mousePos.get(player);
    }

    public static Position getMousePos(Player player, Position defaultValue) {
        return plr2mousePos.getOrDefault(player, defaultValue);
    }
}
