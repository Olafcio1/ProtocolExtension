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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.client.payloads.c2s.KeyPressedC2SPayload;
import pl.olafcio.protocolextension.client.payloads.c2s.MouseMoveC2SPayload;
import pl.olafcio.protocolextension.client.payloads.s2c.*;
import pl.olafcio.protocolextension.client.state.WindowTitle;
import pl.olafcio.protocolextension.client.state.hud.HudElement;
import pl.olafcio.protocolextension.client.state.hud.HudState;

public class Main implements ModInitializer, ClientModInitializer {
    public static MinecraftClient mc;
    public static Logger logger;

    @Override
    public void onInitialize() {
        logger = LoggerFactory.getLogger("ProtocolExtension");

        // C2S
        PayloadTypeRegistry.playC2S().register(KeyPressedC2SPayload.ID, KeyPressedC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MouseMoveC2SPayload.ID, MouseMoveC2SPayload.CODEC);

        // S2C
        PayloadTypeRegistry.playS2C().register(ActivateS2CPayload.ID, ActivateS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ToggleHUDS2CPayload.ID, ToggleHUDS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PutHUDElementS2CPayload.ID, PutHUDElementS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DeleteHUDElementS2CPayload.ID, DeleteHUDElementS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearHUDS2CPayload.ID, ClearHUDS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetWindowTitleS2CPayload.ID, SetWindowTitleS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerCommandS2CPayload.ID, ServerCommandS2CPayload.CODEC);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ToggleHUDS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().options.hudHidden = !payload.state();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ActivateS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                NetworkUtil.enabled = true;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PutHUDElementS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                HudState.elements.put(payload.id(), new HudElement(
                        new Position(payload.x(), payload.y()),
                        payload.text()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DeleteHUDElementS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (HudState.elements.remove(payload.id()) == null)
                    logger.warn("Tried to delete non-existent HUD element");
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClearHUDS2CPayload.ID, (payload, context) -> {
            context.client().execute(HudState.elements::clear);
        });

        ClientPlayNetworking.registerGlobalReceiver(SetWindowTitleS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                WindowTitle.text = payload.title();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerCommandS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().options.sneakKey.setPressed(payload.sneaking());
                context.client().options.sprintKey.setPressed(payload.sprinting());
            });
        });
    }
}
