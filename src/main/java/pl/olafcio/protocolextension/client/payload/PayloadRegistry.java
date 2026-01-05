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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import pl.olafcio.protocolextension.both.Order;
import pl.olafcio.protocolextension.both.UIdentifier;
import pl.olafcio.protocolextension.client.payload.func.Unbound;
import pl.olafcio.protocolextension.client.payload.util.CodecUtil;
import pl.olafcio.protocolextension.client.payload.util.ConstructorUtil;
import pl.olafcio.protocolextension.client.payload.util.PayloadUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

// Should this be an enum, like the util ones?
// I think not, because it's kinda not a utility enum - I'd say rather a class.
// Why? Well, it's a registry, and doesn't end with Util. A class just fits more for me.
public final class PayloadRegistry {
    private PayloadRegistry() {}

    private static final HashMap<String, PayloadRecord<?>> payloads = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Record, G extends CustomPayload> PayloadRecord<?> add(Class<T> record, UIdentifier uid) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var id = Identifier.of(uid.namespace(), uid.path());
        var cpID = new CustomPayload.Id<G>(id);

        PacketCodec<RegistryByteBuf, G> codec;

        var constructor = record.getDeclaredConstructors()[0];
        var types = constructor.getParameterTypes();

        var values = record.getRecordComponents();
        Arrays.sort(values, Comparator.comparingInt(
                x -> x.getAnnotation(Order.class).value()
        ));

        var wrapper = PayloadUtil.customPayload(cpID, values, types);
        var wrapperCo = (Constructor<G>)
                        wrapper.getDeclaredConstructor(types);

        G unit = null;

        if (values.length >= 1) {
            var valuesW = wrapper.getDeclaredMethods();
            var valuesWL = Arrays.stream(valuesW)
                    .filter(x -> !x.getName().equals("getId"))
                    .sorted(Comparator.comparingInt(
                            m -> Arrays.stream(values)
                                        .filter(x -> x.getName().equals(m.getName()))
                                        .findAny()
                                        .orElseThrow()
                                        .getAnnotation(Order.class)
                                        .value()
                    ))
            .toArray(Method[]::new);

            var paramTypes = new ArrayList<Class<?>>();
            var paramValues = new ArrayList<>();
            for (var fComponent : valuesWL) {
                var fCodec = CodecUtil.getPacketCodec(fComponent.getReturnType());
                var fFunction = new Unbound(fComponent);

                paramTypes.add(PacketCodec.class);
                paramTypes.add(Function.class);

                paramValues.add(fCodec);
                paramValues.add(fFunction);
            }

            ConstructorUtil.passConstructor(wrapperCo, paramTypes, paramValues);

            var method = CodecUtil.getMethod(paramTypes);
            var args = paramValues.toArray(Object[]::new);

            codec = (PacketCodec<RegistryByteBuf, G>)
                    method.invoke(null, args);
        } else {
            // TODO: This doesn't require any argument - is there a @SuppressWarnings variant for this?
            codec = PacketCodec.unit(unit = wrapperCo.newInstance());
        }

        var pr = new PayloadRecord<>(cpID, codec, types, wrapperCo, unit);
        payloads.put(record.getName(), pr);
        return pr;
    }

    public static PayloadRecord<?> get(Class<?> klass) {
        return payloads.get(klass.getName());
    }

    public static <T> void handleS2C(Class<T> packet, BiConsumer<T, ClientPlayNetworking.Context> handler) {
        try {
            var pr = payloads.get(packet.getName());
            var prCo = packet.getDeclaredConstructor(pr.types);
            var params = prCo.getParameters();

            ClientPlayNetworking.registerGlobalReceiver(pr.id, (payload, context) -> {
                var provide = new Object[params.length];

                try {
                    var i = 0;
                    for (var param : params) {
                        var method = payload.getClass().getDeclaredMethod(param.getName());
                        var value = method.invoke(payload);

                        provide[i++] = value;
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to scan S2C packet", e);
                }

                T transformed;
                try {
                    transformed = prCo.newInstance(provide);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to type S2C packet", e);
                }

                context.client().execute(() -> handler.accept(transformed, context));
            });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register a S2C packet handler", e);
        }
    }
}
