package pl.olafcio.protocolextension.client.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record KeyPressed(int key) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "key-pressed");

    public static final CustomPayload.Id<KeyPressed> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, KeyPressed> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, KeyPressed::key,
            KeyPressed::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
