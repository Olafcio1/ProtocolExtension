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

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.NotNull;
import pl.olafcio.protocolextension.client.Main;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public enum CodecUtil {
    ;

    private static final HashMap<Class<?>, PacketCodec<?, ?>> typeMap = new HashMap<>(){{
        this.put(String.class, PacketCodecs.STRING);
        this.put(short.class, PacketCodecs.SHORT);
        this.put(int.class, PacketCodecs.INTEGER);
        this.put(double.class, PacketCodecs.DOUBLE);
        this.put(boolean.class, PacketCodecs.BOOLEAN);
    }};

    public static @NotNull PacketCodec<?, ?> getPacketCodec(Class<?> type) {
        var fCodec = typeMap.get(type);
        if (fCodec == null)
            throw new RuntimeException("'" + type.getName() + "' type encountered; cannot get java type's codec");

        return fCodec;
    }

    public static @NotNull Method getMethod(ArrayList<Class<?>> paramTypes) {
        var methods = PacketCodec.class.getMethods();
        Method method = null;
        for (var m : methods) {
            if (
                    m.getName().equals("tuple") &&
                            m.getParameterCount() == paramTypes.size()
            ) {
                method = m;
                break;
            }
        }

        if (method == null)
            throw new RuntimeException("Couldn't construct PX packet: failed to retrieve appropriate tuple(...) variant");

        Main.logger.info("Found {}", method);
        return method;
    }
}
