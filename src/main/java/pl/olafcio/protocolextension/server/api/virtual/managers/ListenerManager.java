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

package pl.olafcio.protocolextension.server.api.virtual.managers;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionListener;

public interface ListenerManager {
    //#region Registration

    /**
     * Registers a listener.
     * <p>
     * A listener is a class that implements the {@code ProtocolExtensionListener} interface.<br/>
     * For example:
     * <pre>{@code
     * public class ExamplePXListener implements ProtocolExtensionListener {
     *     public void onKeyPressed(Player player, int key) {
     *         if (key == 72) {
     *             player.sendMessage(Component.text("You pressed H!"));
     *         }
     *     }
     * }
     * }</pre>
     */
    // TODO: Key constants server-side
    void registerListener(ProtocolExtensionListener listener);

    /**
     * Unregisters a listener.
     */
    boolean unregisterListener(ProtocolExtensionListener listener);

    //#region Dispatching events
    void dispatchEvent(String methodName, Player player, Class<?>[] types, Object[] values);

    default void dispatchEvent(String methodName, PacketReceiveEvent event, Class<?>[] types, Object[] values) {
        dispatchEvent(methodName, (Player) event.getPlayer(), types, values);
    }

    default void dispatchEvent(String methodName, PlayerEvent event, Class<?>[] types, Object[] values) {
        dispatchEvent(methodName, event.getPlayer(), types, values);
    }
}
