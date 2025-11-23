package pl.olafcio.protocolextension.client.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToggleHUD(boolean state) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("protocolextension", "toggle-hud");

    public static final CustomPayload.Id<ToggleHUD> ID = new CustomPayload.Id<>(ID_RAW);
    public static final PacketCodec<RegistryByteBuf, ToggleHUD> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ToggleHUD::state,
            ToggleHUD::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
