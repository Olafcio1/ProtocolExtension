package pl.olafcio.protocolextension.client.payloads.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleHUDS2CPayload(boolean state) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "toggle-hud");

    public static final CustomPayload.Id<ToggleHUDS2CPayload> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, ToggleHUDS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ToggleHUDS2CPayload::state,
            ToggleHUDS2CPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
