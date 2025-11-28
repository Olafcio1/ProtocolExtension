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
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Don't convert to a record; it makes it even harder to see the methods in autocompletion
public final class PayloadRecord<T extends CustomPayload> {
    public final CustomPayload.Id<T> id;
    public final PacketCodec<RegistryByteBuf, T> codec;
    public final Class<?>[] types;
    public final Constructor<CustomPayload> constructor;

    public PayloadRecord(CustomPayload.Id<T> id, PacketCodec<RegistryByteBuf, T> codec, Class<?>[] types, Constructor<CustomPayload> constructor) {
        this.id = id;
        this.codec = codec;
        this.types = types;
        this.constructor = constructor;
    }

    @SuppressWarnings("unchecked")
    public T create(Object... values) {
        try {
            return (T) constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create packet", e);
        }
    }

    public void registerS2C() {
        PayloadTypeRegistry.playS2C().register(id, codec);
    }
    public void registerC2S() {
        PayloadTypeRegistry.playC2S().register(id, codec);
    }
}
