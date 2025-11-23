package pl.olafcio.protocolextension.client.payloads.c2s;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KeyPressedC2SPayload(int key) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "key-pressed");

    public static final CustomPayload.Id<KeyPressedC2SPayload> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, KeyPressedC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, KeyPressedC2SPayload::key,
            KeyPressedC2SPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
