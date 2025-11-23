package pl.olafcio.protocolextension.client;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;

public enum NetworkUtil {
    ;

    public static boolean enabled = false;
    public static void send(CustomPayload payload) {
        if (enabled)
            Main.mc.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(payload));
    }
}
