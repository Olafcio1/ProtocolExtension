package pl.olafcio.protocolextension.client.payloads.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ActivateS2CPayload() implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "activate");

    public static final CustomPayload.Id<ActivateS2CPayload> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, ActivateS2CPayload> CODEC = PacketCodec.unit(new ActivateS2CPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
