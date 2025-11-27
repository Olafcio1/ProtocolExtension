/*
 * Copyright (c) 2025 Olafcio
 * (Olafcio1 on GitHub)
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package pl.olafcio.protocolextension.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ServerInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.olafcio.protocolextension.client.Main;
import pl.olafcio.protocolextension.client.NetworkUtil;
import pl.olafcio.protocolextension.client.state.MoveState;
import pl.olafcio.protocolextension.client.state.WindowTitle;
import pl.olafcio.protocolextension.client.state.hud.HudState;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public abstract ServerInfo getCurrentServerEntry();

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(RunArgs args, CallbackInfo ci) {
        Main.mc = (MinecraftClient) (Object) this;
    }

    @Inject(at = @At("RETURN"), method = "getWindowTitle", cancellable = true, order = 1001)
    public void getWindowTitle(CallbackInfoReturnable<String> cir) {
        // Choosing this implementation over more optimized solutions,
        // because other mods can change the title too.
        if (WindowTitle.text != null) {
            var server = getCurrentServerEntry();
            assert server != null;

            var original = cir.getReturnValue();
            var string =
                    original.split(" -", 2)[0] +
                    " - " +
                    StringUtils.capitalize(server.name) +
                    " - " +
                    WindowTitle.text;

            cir.setReturnValue(string);
        }
    }

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    public void onDisconnected(CallbackInfo ci) {
        NetworkUtil.enabled = false;

        MoveState.value = true;
        WindowTitle.text = null;

        HudState.hotbar = true;
        HudState.elements.clear();
    }
}
