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

package pl.olafcio.protocolextension.client.payload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Don't convert to a record; it makes it even harder to see the methods in autocompletion
public final class PayloadRecord<T extends CustomPacketPayload> {
    public final CustomPacketPayload.Type<T> id;
    public final StreamCodec<RegistryFriendlyByteBuf, T> codec;
    public final Class<?>[] types;
    public final Constructor<T> constructor;
    public final T unit;

    public PayloadRecord(
            CustomPacketPayload.Type<T> id,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            Class<?>[] types,
            Constructor<T> constructor,
            T unit
    ) {
        this.id = id;
        this.codec = codec;
        this.types = types;
        this.constructor = constructor;
        this.unit = unit;
    }

    public T create(Object... values) {
        try {
            if (this.unit == null)
                return constructor.newInstance(values);
            else return this.unit;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create packet", e);
        }
    }

    public PayloadRecord<T> registerS2C() {
        PayloadTypeRegistry.clientboundPlay().register(id, codec);
        return this;
    }

    public PayloadRecord<T> registerC2S() {
        PayloadTypeRegistry.serverboundPlay().register(id, codec);
        return this;
    }
}
