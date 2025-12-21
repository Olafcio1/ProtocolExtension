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

package pl.olafcio.protocolextension.server;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class VariableAPI implements ProtocolExtensionListener {
    private static final ArrayList<Player> activatedPlayers = new ArrayList<>();
    private static final HashMap<Player, Position> plr2mousePos = new HashMap<>();

    @Override
    public void onMouseMove(Player player, double x, double y) {
        plr2mousePos.put(player, new Position(x, y));
    }

    @Override
    public void onActivated(Player player) {
        activatedPlayers.add(player);
    }

    @Override
    public void onDisconnect(Player player) {
        plr2mousePos.remove(player);
        activatedPlayers.remove(player);
    }

    public static @Nullable Position getMousePos(Player player) {
        return plr2mousePos.get(player);
    }

    public static Position getMousePos(Player player, Position defaultValue) {
        return plr2mousePos.getOrDefault(player, defaultValue);
    }

    /**
     * Returns a list of online players that use ProtocolExtension.
     */
    public static List<Player> getActivatedPlayers() {
        return activatedPlayers;
    }

    public static boolean isActivated(Player player) {
        return activatedPlayers.contains(player);
    }
}
