package pl.olafcio.protocolextension.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import pl.olafcio.protocolextension.client.packets.KeyPressed;
import pl.olafcio.protocolextension.client.packets.ToggleHUD;

public class Main implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(KeyPressed.ID, KeyPressed.CODEC);
        PayloadTypeRegistry.playS2C().register(ToggleHUD.ID, ToggleHUD.CODEC);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ToggleHUD.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().options.hudHidden = !payload.state();
            });
        });
    }
}
