package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.client.Main;
import pl.olafcio.protocolextension.client.NetworkUtil;
import pl.olafcio.protocolextension.client.payloads.c2s.KeyPressedC2SPayload;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(at = @At("HEAD"), method = "onKey")
    public void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (
                NetworkUtil.enabled &&
                action == GLFW.GLFW_PRESS &&
                input.key() != GLFW.GLFW_KEY_ESCAPE &&
                !input.hasCtrl() &&
                !input.hasAlt() &&
                !input.hasShift() &&
                Main.mc.currentScreen == null
        ) {
            NetworkUtil.send(new KeyPressedC2SPayload(input.key()));
        }
    }
}
