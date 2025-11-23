package pl.olafcio.protocolextension.client.payloads.s2c;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Range;

public record PutHUDElementS2CPayload(
        short id,
        @Range(from = 0, to = 1) double x,
        @Range(from = 0, to = 1) double y,
        String text
) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "put-hud");

    public static final CustomPayload.Id<PutHUDElementS2CPayload> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, PutHUDElementS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.SHORT, PutHUDElementS2CPayload::id,
            PacketCodecs.DOUBLE, PutHUDElementS2CPayload::x,
            PacketCodecs.DOUBLE, PutHUDElementS2CPayload::y,
            PacketCodecs.STRING, PutHUDElementS2CPayload::text,
            PutHUDElementS2CPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
