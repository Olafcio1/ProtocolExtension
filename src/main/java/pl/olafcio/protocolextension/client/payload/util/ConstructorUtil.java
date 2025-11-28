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

import com.mojang.datafixers.util.*;
import pl.olafcio.protocolextension.client.payload.func.VarFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum ConstructorUtil {
    ;

    private static final Class<?>[] functionTypes = new Class<?>[]{
            Function.class,
            BiFunction.class,
            Function3.class,
            Function4.class,
            Function5.class,
            Function6.class,
            Function7.class,
            Function8.class,
            Function9.class,
            Function10.class,
            Function11.class,
            Function12.class,
            Function13.class,
            Function14.class,
            Function15.class,
            Function16.class
    };

    public static <T> void passConstructor(Constructor<T> constructor, ArrayList<Class<?>> paramTypes, ArrayList<Object> paramValues) {
        var params = constructor.getParameterCount();
        var callback = (VarFunction<T>) varargs -> {
            try {
                return constructor.newInstance(varargs);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to construct payload", e);
            }
        };

        // It's sadly impossible to shorten this without some weirdahh ByteBuddy/ASM magic that there's already way too
        // much in this project
        var klass = functionTypes[params - 1];
        paramTypes.add(klass);
        paramValues.add(switch (params) {
            case 1 -> (Function<?, ?>) callback::apply;
            case 2 -> (BiFunction<?, ?, ?>) callback::apply;
            case 3 -> (Function3<?, ?, ?, ?>) callback::apply;
            case 4 -> (Function4<?, ?, ?, ?, ?>) callback::apply;
            case 5 -> (Function5<?, ?, ?, ?, ?, ?>) callback::apply;
            case 6 -> (Function6<?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 7 -> (Function7<?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 8 -> (Function8<?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 9 -> (Function9<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 10 -> (Function10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 11 -> (Function11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 12 -> (Function12<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 13 -> (Function13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 14 -> (Function14<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 15 -> (Function15<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            case 16 -> (Function16<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply;
            default -> throw new IllegalStateException("Unexpected value: " + params);
        });
    }
}
