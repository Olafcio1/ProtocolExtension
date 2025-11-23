package pl.olafcio.protocolextension.server.api.packetevents;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import io.netty.buffer.Unpooled;
import pl.olafcio.protocolextension.server.ProtocolExtension;

public class ProtocolExtensionPacketEventsListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            var wrapper = new WrapperPlayClientPluginMessage(event);
            var channel = wrapper.getChannelName();

            if (channel.equals("protocolextension:key-pressed")) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());

                ProtocolExtension.getAPI().dispatchEvent(
                        "onKeyPressed",
                        event.getPlayer(),
                        new Class<?>[]{
                                int.class
                        },
                        new Object[]{
                                data.readInt()
                        }
                );
            } else if (channel.equals("protocolextension:mouse-move")) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());

                ProtocolExtension.getAPI().dispatchEvent(
                        "onMouseMove",
                        event.getPlayer(),
                        new Class<?>[]{
                                double.class,
                                double.class
                        },
                        new Object[]{
                                data.readDouble(),
                                data.readDouble()
                        }
                );
            }
        }
    }
}
