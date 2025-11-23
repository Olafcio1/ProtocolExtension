package pl.olafcio.protocolextension.server.api.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import pl.olafcio.protocolextension.server.api.API;
import pl.olafcio.protocolextension.server.api.PacketConstructionError;

public class PacketEventsAPI implements API {
    protected PacketWrapper<?> make(String type, Object... codec) {
        var buf = Unpooled.buffer();
        for (var obj : codec)
            if (obj instanceof Boolean bool)
                buf.writeBoolean(bool);
            else throw new PacketConstructionError("Cannot encode type '" + obj.getClass().getName() + "'");

        var data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return new WrapperPlayServerPluginMessage(type, data);
    }

    public void forceHUD(User player, boolean state) {
        player.sendPacket(make(
                "protocolextension:toggle-hud",
                state
        ));
    }

    @Override
    public void forceHUD(Player player, boolean state) {
        forceHUD(PacketEvents.getAPI().getPlayerManager().getUser(player), state);
    }
}
