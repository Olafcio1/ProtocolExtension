package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.client.Main;
import pl.olafcio.protocolextension.client.NetworkUtil;
import pl.olafcio.protocolextension.client.payloads.c2s.MouseMoveC2SPayload;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;y:D", shift = At.Shift.AFTER), method = "onCursorPos")
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        final var screen = Main.mc.currentScreen;
        if (
                NetworkUtil.enabled &&
                screen != null &&
                !(screen instanceof GameMenuScreen)
        ) {
            NetworkUtil.send(new MouseMoveC2SPayload(
                    x / (double)screen.width,
                    y / (double)screen.height
            ));
        }
    }
}
