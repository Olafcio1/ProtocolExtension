package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.client.packets.KeyPressed;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(at = @At("HEAD"), method = "onKey")
    public void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        final var mc = MinecraftClient.getInstance();
        if (
                action == GLFW.GLFW_PRESS &&
                input.key() != GLFW.GLFW_KEY_ESCAPE &&
                !input.hasCtrl() &&
                !input.hasAlt() &&
                !input.hasShift() &&
                mc.currentScreen == null
        ) {
            mc.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(new KeyPressed(input.key())));
        }
    }
}
