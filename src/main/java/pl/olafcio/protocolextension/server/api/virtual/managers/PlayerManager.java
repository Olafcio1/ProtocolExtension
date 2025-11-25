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

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface PlayerManager {
    /**
     * Used to enable ProtocolExtension support in the client.
     * Should be called only once.
     */
    void activate(Player player);

    /**
     * Forces the HUD visibility state onto the client.
     */
    void forceHUD(Player player, boolean state);

    /**
     * Adds or modifies a HUD element of the given ID in the client.
     */
    void putHUD(Player player, short id, double x, double y, Component text);

    /**
     * Deletes a HUD element of the given ID in the client.
     */
    void deleteHUD(Player player, short id);

    /**
     * Deletes all HUD elements in the client.
     */
    void clearHUD(Player player);

    /**
     * Sets the window title in the client.
     * <p>
     * Due to security reasons, there is text prepended to the `text` parameter, for example:
     * <pre>{@code
     * @EventHandler
     * public void onPlayerJoin(PlayerJoinEvent event) {
     *     ProtocolExtension.getAPI().setWindowTitle(event.getPlayer(), Component.text("Boss fight"));
     * }
     * }</pre>
     *
     * may result in the following text:
     * <pre>{@code
     * Minecraft* 1.21.10 - Localhost - Boss fight
     * }</pre>
     */
    void setWindowTitle(Player player, Component text);

    /**
     * Forces a sneaking state & sprinting state onto the client.
     */
    void serverCommand(Player player, boolean sneaking, boolean sprinting);
}
