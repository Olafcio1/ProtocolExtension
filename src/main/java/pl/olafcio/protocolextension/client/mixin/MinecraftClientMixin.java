package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.client.Main;
import pl.olafcio.protocolextension.client.NetworkUtil;
import pl.olafcio.protocolextension.client.state.hud.HudState;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(RunArgs args, CallbackInfo ci) {
        Main.mc = (MinecraftClient) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    public void onDisconnected(CallbackInfo ci) {
        NetworkUtil.enabled = false;
        HudState.elements.clear();
    }
}
