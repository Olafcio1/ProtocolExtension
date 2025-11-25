/*
 * Copyright (c) 2025 Olafcio
 * (Olafcio1 on GitHub)
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package pl.olafcio.protocolextension.server.api.player.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pl.olafcio.protocolextension.server.api.PacketConstructionError;
import pl.olafcio.protocolextension.server.api.virtual.managers.PlayerManager;

public class ProtocolExtensionPacketEventsPlayerManager implements PlayerManager {
    //#region Packets
    public enum Packets {
        ;

        @Contract("_, _ -> new")
        public static @NotNull PacketWrapper<?> make(String type, @NotNull Object... codec) {
            var buf = new PacketWrapper<>(0);
            buf.setBuffer(Unpooled.buffer());

            for (var obj : codec)
                switch (obj) {
                    case Boolean bool -> buf.writeBoolean(bool);
                    case Short s -> buf.writeShort(s);
                    case Float f -> buf.writeFloat(f);
                    case Double d -> buf.writeDouble(d);
                    case String s -> buf.writeString(s);
                    case null, default ->
                            throw new PacketConstructionError("Cannot encode type '" + obj.getClass().getName() + "'");
                }

            var data = buf.readRemainingBytes();
            return new WrapperPlayServerPluginMessage(type, data);
        }
    }

    //#region User methods
    public enum UserMethods {
        ;

        public static void activate(User player) {
            player.sendPacket(Packets.make(
                    "protocolextension:activate"
            ));
        }

        public static void forceHUD(User player, boolean state) {
            player.sendPacket(Packets.make(
                    "protocolextension:toggle-hud",
                    state
            ));
        }

        public static void putHUD(User player, short id, double x, double y, Component text) {
            player.sendPacket(Packets.make(
                    "protocolextension:put-hud",
                    id,
                    x,
                    y,
                    LegacyComponentSerializer.legacySection().serialize(text)
            ));
        }

        public static void deleteHUD(User player, short id) {
            player.sendPacket(Packets.make(
                    "protocolextension:delete-hud",
                    id
            ));
        }

        public static void clearHUD(User player) {
            player.sendPacket(Packets.make(
                    "protocolextension:clear-hud"
            ));
        }

        public static void setWindowTitle(User player, Component text) {
            player.sendPacket(Packets.make(
                    "protocolextension:set-window-title",
                    LegacyComponentSerializer.legacySection().serialize(text)
            ));
        }

        public static void serverCommand(User player, boolean sneaking, boolean sprinting) {
            player.sendPacket(Packets.make(
                    "protocolextension:server-command",
                    sneaking,
                    sprinting
            ));
        }
    }

    //#region Player methods
    @Override
    public void activate(Player player) {
        UserMethods.activate(PacketEvents.getAPI().getPlayerManager().getUser(player));
    }

    @Override
    public void forceHUD(Player player, boolean state) {
        UserMethods.forceHUD(PacketEvents.getAPI().getPlayerManager().getUser(player), state);
    }

    @Override
    public void putHUD(Player player, short id, double x, double y, Component text) {
        UserMethods.putHUD(PacketEvents.getAPI().getPlayerManager().getUser(player), id, x, y, text);
    }

    @Override
    public void deleteHUD(Player player, short id) {
        UserMethods.deleteHUD(PacketEvents.getAPI().getPlayerManager().getUser(player), id);
    }

    @Override
    public void clearHUD(Player player) {
        UserMethods.clearHUD(PacketEvents.getAPI().getPlayerManager().getUser(player));
    }

    @Override
    public void setWindowTitle(Player player, Component text) {
        UserMethods.setWindowTitle(PacketEvents.getAPI().getPlayerManager().getUser(player), text);
    }

    @Override
    public void serverCommand(Player player, boolean sneaking, boolean sprinting) {
        UserMethods.serverCommand(PacketEvents.getAPI().getPlayerManager().getUser(player), sneaking, sprinting);
    }
}
