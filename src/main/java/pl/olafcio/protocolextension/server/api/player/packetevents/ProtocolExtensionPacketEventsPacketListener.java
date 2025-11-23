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
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import io.netty.buffer.Unpooled;
import pl.olafcio.protocolextension.server.ProtocolExtension;

public class ProtocolExtensionPacketEventsPacketListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            var wrapper = new WrapperPlayClientPluginMessage(event);
            var channel = wrapper.getChannelName();

            if (channel.equals("protocolextension:key-pressed")) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());

                ProtocolExtension.getAPI().listenerManager().dispatchEvent(
                        "onKeyPressed",
                        event,
                        new Class<?>[]{
                                int.class
                        },
                        new Object[]{
                                data.readInt()
                        }
                );
            } else if (channel.equals("protocolextension:mouse-move")) {
                var data = Unpooled.wrappedBuffer(wrapper.getData());

                ProtocolExtension.getAPI().listenerManager().dispatchEvent(
                        "onMouseMove",
                        event,
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
