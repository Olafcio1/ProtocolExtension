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
//                !input.hasCtrl() &&
                !input.hasAlt() &&
//                !input.hasShift() &&
                Main.mc.currentScreen == null
        ) {
            NetworkUtil.send(new KeyPressedC2SPayload(input.key()));
        }
    }
}
