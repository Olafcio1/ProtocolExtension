package pl.olafcio.protocolextension.client.payloads.c2s;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MouseMoveC2SPayload(double x, double y) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "mouse-move");

    public static final CustomPayload.Id<MouseMoveC2SPayload> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, MouseMoveC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, MouseMoveC2SPayload::x,
            PacketCodecs.DOUBLE, MouseMoveC2SPayload::y,
            MouseMoveC2SPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
