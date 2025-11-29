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

package pl.olafcio.protocolextension.client.payload.util;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.TypeManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;
import pl.olafcio.protocolextension.client.Main;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

public enum PayloadUtil {
    ;

    public static @NotNull Class<?> customPayload(CustomPayload.Id<? extends CustomPayload> cpID, RecordComponent[] values, Class<?>[] types) {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .implement(CustomPayload.class)
                .modifiers(Visibility.PUBLIC, TypeManifestation.FINAL);

        for (var param : values) {
            var name = param.getName();
            var type = param.getType();

            builder = builder
                    .defineField(name, type, Visibility.PRIVATE, FieldManifestation.FINAL)
                    .defineMethod(name, type, Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField(name));
        }

        builder = builder.method(ElementMatchers.named("getId"))
                .intercept(FixedValue.value(cpID));

        if (types.length >= 1) {
            var wrapperCo = builder.defineConstructor(Modifier.PUBLIC);

            builder = wrapperCo
                    .withParameters(types)
                    .intercept(ImplementationUtil.getImplementation(values));
        }

        return builder
                .make()
                .load(Main.class.getClassLoader())
                .getLoaded();
    }
}
