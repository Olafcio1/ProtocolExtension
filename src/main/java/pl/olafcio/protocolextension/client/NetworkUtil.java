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

package pl.olafcio.protocolextension.client;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import pl.olafcio.protocolextension.client.state.GameState;
import pl.olafcio.protocolextension.client.state.MoveState;
import pl.olafcio.protocolextension.client.state.WindowTitle;
import pl.olafcio.protocolextension.client.state.hud.HudState;

public enum NetworkUtil {
    ;

    public static boolean enabled = false;
    public static void send(CustomPacketPayload payload) {
        if (enabled)
            Main.mc.player.connection.send(new ServerboundCustomPayloadPacket(payload));
    }

    public static void reset() {
        enabled = false;

        MoveState.value = true;
        GameState.render = true;

        WindowTitle.text = null;

        HudState.hotbar = true;
        HudState.elements.clear();
    }
}
