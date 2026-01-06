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

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import io.netty.buffer.Unpooled;
import pl.olafcio.protocolextension.both.payloads.ActivatePayload;
import pl.olafcio.protocolextension.both.payloads.c2s.KeyPressedC2SPayload;
import pl.olafcio.protocolextension.both.payloads.c2s.MouseMoveC2SPayload;
import pl.olafcio.protocolextension.server.ProtocolExtension;
import pl.olafcio.protocolextension.server.util.ArrayUtils;

public class ProtocolExtensionPacketEventsPacketListener implements PacketListener {
    private final Class<?>[] KP_TYPES = new Class<?>[]{
            int.class
    };

    private final Class<?>[] MM_TYPES = new Class<?>[]{
            double.class,
            double.class
    };

    private final Object[] SINGLE_ARRAY = new Object[1];
    private final Object[] DOUBLE_ARRAY = new Object[2];

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            var wrapper = new WrapperPlayClientPluginMessage(event);
            var channel = wrapper.getChannelName();

            if (channel.equals(ActivatePayload.ID.toString())) {
                ProtocolExtension.getAPI().playerManager().activate(
                        event.getPlayer()
                );

                ProtocolExtension.getAPI().listenerManager().dispatchEvent(
                        "onActivated",
                        event,
                        ArrayUtils.EMPTY,
                        ArrayUtils.EMPTY
                );
            } else if (channel.equals(KeyPressedC2SPayload.ID.toString())) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());
                SINGLE_ARRAY[0] = data.readInt();

                ProtocolExtension.getAPI().listenerManager().dispatchEvent(
                        "onKeyPressed",
                        event,
                        KP_TYPES,
                        SINGLE_ARRAY
                );
            } else if (channel.equals(MouseMoveC2SPayload.ID.toString())) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());
                DOUBLE_ARRAY[0] = data.readDouble();
                DOUBLE_ARRAY[1] = data.readDouble();

                ProtocolExtension.getAPI().listenerManager().dispatchEvent(
                        "onMouseMove",
                        event,
                        MM_TYPES,
                        DOUBLE_ARRAY
                );
            }
        }
    }
}
