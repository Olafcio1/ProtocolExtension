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

import net.minecraft.client.Keyboard;
//? if >1.21.8 {
/*import net.minecraft.client.input.KeyInput;
*///?}
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.olafcio.protocolextension.both.payloads.c2s.KeyPressedC2SPayload;
import pl.olafcio.protocolextension.client.Main;
import pl.olafcio.protocolextension.client.NetworkUtil;
import pl.olafcio.protocolextension.client.payload.PayloadRegistry;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    //? if <=1.21.8 {
    @Inject(at = @At("HEAD"), method = "onKey")
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        onKey(action, key, (modifiers & GLFW.GLFW_MOD_ALT) == GLFW.GLFW_MOD_ALT);
    }
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "onKey")
    public void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        onKey(action, input.key(), input.hasAlt());
    }
    *///?}

    @Unique
    private void onKey(int action, int key, boolean hasAlt) {
        if (
                NetworkUtil.enabled &&
                action == GLFW.GLFW_PRESS &&
                key != GLFW.GLFW_KEY_ESCAPE &&
                key != GLFW.GLFW_KEY_UNKNOWN &&
                // Tf are world keys lmfao
                key != GLFW.GLFW_KEY_WORLD_1 &&
                key != GLFW.GLFW_KEY_WORLD_2 &&
//              !input.hasCtrl() &&
                !hasAlt &&
//              !input.hasShift() &&
                Main.mc.currentScreen == null
        ) {
            NetworkUtil.send(PayloadRegistry.get(KeyPressedC2SPayload.class).create(key));
        }
    }
}
