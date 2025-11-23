package pl.olafcio.protocolextension.client.payloads.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DeleteHUDElementS2CPayload(
        short id
) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "delete-hud");

    public static final Id<DeleteHUDElementS2CPayload> ID = new Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, DeleteHUDElementS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.SHORT, DeleteHUDElementS2CPayload::id,
            DeleteHUDElementS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
