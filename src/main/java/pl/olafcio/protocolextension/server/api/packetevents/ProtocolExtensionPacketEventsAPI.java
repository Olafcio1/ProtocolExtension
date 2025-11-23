package pl.olafcio.protocolextension.server.api.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.NbtCompound;
import org.bukkit.entity.Player;
import pl.olafcio.protocolextension.server.api.virtual.API;
import pl.olafcio.protocolextension.server.api.PacketConstructionError;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolExtensionPacketEventsAPI implements API {
    //#region Packets
    public enum Packets {
        ;

        public static PacketWrapper<?> make(String type, Object... codec) {
            var buf = new PacketWrapper<>(0);
            buf.setBuffer(Unpooled.buffer());

            for (var obj : codec)
                if (obj instanceof Boolean bool)
                    buf.writeBoolean(bool);
                else if (obj instanceof Short s)
                    buf.writeShort(s);
                else if (obj instanceof Float f)
                    buf.writeFloat(f);
                else if (obj instanceof Double d)
                    buf.writeDouble(d);
                else if (obj instanceof String s)
                    buf.writeString(s);
                else throw new PacketConstructionError("Cannot encode type '" + obj.getClass().getName() + "'");

            var data = buf.readRemainingBytes();
            return new WrapperPlayServerPluginMessage(type, data);
        }
    }

    //#region User methods
    public enum UserMethods {
        ;

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
    }

    //#region Listeners
    private final ArrayList<ProtocolExtensionListener> listeners = new ArrayList<>();

    @Override
    public void registerListener(ProtocolExtensionListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean unregisterListener(ProtocolExtensionListener listener) {
        return listeners.remove(listener);
    }

    //#region Listener events
    @Override
    public void dispatchEvent(String methodName, Player player, Class<?>[] types, Object[] values) {
        var typeList = new ArrayList<Class<?>>();
        typeList.add(Player.class);
        typeList.addAll(List.of(types));

        var valueList = new ArrayList<>();
        valueList.add(player);
        valueList.addAll(List.of(values));

        var typeArray = typeList.toArray(Class<?>[]::new);
        var valueArray = valueList.toArray(Object[]::new);

        try {
            for (var listener : listeners) {
                var method = listener.getClass().getDeclaredMethod(methodName, typeArray);
                method.invoke(listener, valueArray);
            }
        } catch (RuntimeException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //#region Player methods
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
}
