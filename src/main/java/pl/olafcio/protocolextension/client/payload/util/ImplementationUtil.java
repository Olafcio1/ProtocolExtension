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

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;

import static net.bytebuddy.jar.asm.Opcodes.*;

public enum ImplementationUtil {
    ;

    private static final HashMap<String, Integer> TYPE_MAP = new HashMap<>() {{
        this.put(int.class.getName(), ILOAD);
        this.put(boolean.class.getName(), ILOAD);
        this.put(byte.class.getName(), ILOAD);
        this.put(short.class.getName(), ILOAD);
        this.put(char.class.getName(), ILOAD);

        this.put(long.class.getName(), LLOAD);
        this.put(float.class.getName(), FLOAD);
        this.put(double.class.getName(), DLOAD);
    }};

    public static @NotNull Implementation.Simple getImplementation(RecordComponent[] values) {
        return new Implementation.Simple((mv, ctx, method) -> {
            int size = 1;

            // --- call super() ---
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V",
                    false);

            // --- assignments ---
            for (var param : values) {
                var type = param.getType();
                var typeLoad = TYPE_MAP.getOrDefault(type.getName(), ALOAD);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(typeLoad, size);
                mv.visitFieldInsn(PUTFIELD,
                        ctx.getInstrumentedType().getInternalName(),
                        param.getName(),
                        type.descriptorString());

                if (type == long.class || type == double.class)
                    size += 2;
                else size++;
            }

            mv.visitInsn(RETURN);

            return new ByteCodeAppender.Size(size, size);
        });
    }
}
