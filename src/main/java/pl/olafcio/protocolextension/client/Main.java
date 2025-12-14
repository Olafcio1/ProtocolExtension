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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.both.payloads.ActivatePayload;
import pl.olafcio.protocolextension.both.payloads.c2s.*;
import pl.olafcio.protocolextension.both.payloads.s2c.*;
import pl.olafcio.protocolextension.client.payload.*;
import pl.olafcio.protocolextension.client.state.MoveState;
import pl.olafcio.protocolextension.client.state.WindowTitle;
import pl.olafcio.protocolextension.client.state.hud.HudElement;
import pl.olafcio.protocolextension.client.state.hud.HudState;

import java.lang.reflect.*;

public class Main implements ModInitializer, ClientModInitializer {
    public static MinecraftClient mc;
    public static Logger logger;

    @Override
    public void onInitialize() {
        logger = LoggerFactory.getLogger("ProtocolExtension");

        // C2S
        try {
            PayloadRegistry.add(ActivatePayload.class, ActivatePayload.ID).registerC2S().registerS2C();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to add activate packet", e);
        }

        try {
            PayloadRegistry.add(KeyPressedC2SPayload.class, KeyPressedC2SPayload.ID).registerC2S();
            PayloadRegistry.add(MouseMoveC2SPayload.class, MouseMoveC2SPayload.ID).registerC2S();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to add C2S packets", e);
        }

        // S2C
        try {
            PayloadRegistry.add(HUDToggleS2CPayload.class, HUDToggleS2CPayload.ID).registerS2C();
            PayloadRegistry.add(HUDPutElementS2CPayload.class, HUDPutElementS2CPayload.ID).registerS2C();
            PayloadRegistry.add(HUDDeleteElementS2CPayload.class, HUDDeleteElementS2CPayload.ID).registerS2C();
            PayloadRegistry.add(HUDClearS2CPayload.class, HUDClearS2CPayload.ID).registerS2C();
            PayloadRegistry.add(SetWindowTitleS2CPayload.class, SetWindowTitleS2CPayload.ID).registerS2C();
            PayloadRegistry.add(SetPerspectiveS2CPayload.class, SetPerspectiveS2CPayload.ID).registerS2C();
            PayloadRegistry.add(ServerCommandS2CPayload.class, ServerCommandS2CPayload.ID).registerS2C();
            PayloadRegistry.add(MoveToggleS2CPayload.class, MoveToggleS2CPayload.ID).registerS2C();
            PayloadRegistry.add(HUDSettingHotbarS2CPayload.class, HUDSettingHotbarS2CPayload.ID).registerS2C();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to add S2C packets", e);
        }
    }

    @Override
    public void onInitializeClient() {
        PayloadRegistry.handleS2C(ActivatePayload.class, (payload, context) -> {
            NetworkUtil.enabled = true;
        });

        PayloadRegistry.handleS2C(HUDToggleS2CPayload.class, (payload, context) -> {
            context.client().options.hudHidden = !payload.state();
        });

        PayloadRegistry.handleS2C(HUDPutElementS2CPayload.class, (payload, context) -> {
            HudState.elements.put(payload.id(), new HudElement(
                    new Position(payload.x(), payload.y()),
                    payload.text()
            ));
        });

        PayloadRegistry.handleS2C(HUDDeleteElementS2CPayload.class, (payload, context) -> {
            if (HudState.elements.remove(payload.id()) == null)
                logger.warn("Tried to delete non-existent HUD element");
        });

        PayloadRegistry.handleS2C(HUDClearS2CPayload.class, (payload, context) -> {
            HudState.elements.clear();
        });

        PayloadRegistry.handleS2C(SetWindowTitleS2CPayload.class, (payload, context) -> {
            WindowTitle.text = payload.title();
        });

        var perspectives = Perspective.values();
        PayloadRegistry.handleS2C(SetPerspectiveS2CPayload.class, (payload, context) -> {
            mc.options.setPerspective(perspectives[payload.person()]);
        });

        PayloadRegistry.handleS2C(ServerCommandS2CPayload.class, (payload, context) -> {
            context.client().options.sneakKey.setPressed(payload.sneaking());
            context.client().options.sprintKey.setPressed(payload.sprinting());
        });

        PayloadRegistry.handleS2C(MoveToggleS2CPayload.class, (payload, context) -> {
            MoveState.value = payload.canMove();
        });

        PayloadRegistry.handleS2C(HUDSettingHotbarS2CPayload.class, (payload, context) -> {
            HudState.hotbar = payload.shown();
        });
    }
}
