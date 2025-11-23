package pl.olafcio.protocolextension.client;

import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtParsing;
import net.minecraft.util.packrat.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.client.payloads.c2s.KeyPressedC2SPayload;
import pl.olafcio.protocolextension.client.payloads.c2s.MouseMoveC2SPayload;
import pl.olafcio.protocolextension.client.payloads.s2c.ActivateS2CPayload;
import pl.olafcio.protocolextension.client.payloads.s2c.DeleteHUDElementS2CPayload;
import pl.olafcio.protocolextension.client.payloads.s2c.PutHUDElementS2CPayload;
import pl.olafcio.protocolextension.client.payloads.s2c.ToggleHUDS2CPayload;
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
        PayloadTypeRegistry.playS2C().register(ToggleHUDS2CPayload.ID, ToggleHUDS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ActivateS2CPayload.ID, ActivateS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PutHUDElementS2CPayload.ID, PutHUDElementS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DeleteHUDElementS2CPayload.ID, DeleteHUDElementS2CPayload.CODEC);
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
    }
}
