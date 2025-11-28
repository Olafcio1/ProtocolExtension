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

package pl.olafcio.protocolextension.client;

import com.mojang.datafixers.util.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.TypeManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.matcher.ElementMatchers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.both.UIdentifier;
import pl.olafcio.protocolextension.both.payloads.s2c.*;
import pl.olafcio.protocolextension.client.payloads.c2s.KeyPressedC2SPayload;
import pl.olafcio.protocolextension.client.payloads.c2s.MouseMoveC2SPayload;
import pl.olafcio.protocolextension.client.state.MoveState;
import pl.olafcio.protocolextension.client.state.WindowTitle;
import pl.olafcio.protocolextension.client.state.hud.HudElement;
import pl.olafcio.protocolextension.client.state.hud.HudState;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.bytebuddy.jar.asm.Opcodes.*;

public class Main implements ModInitializer, ClientModInitializer {
    public static MinecraftClient mc;
    public static Logger logger;

    @Override
    public void onInitialize() {
        logger = LoggerFactory.getLogger("ProtocolExtension");

        // C2S
        PayloadTypeRegistry.playC2S().register(KeyPressedC2SPayload.ID, KeyPressedC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MouseMoveC2SPayload.ID, MouseMoveC2SPayload.CODEC);

        // S2C
        try {
            addPacket(ActivateS2CPayload.class, ActivateS2CPayload.ID).registerS2C();
            addPacket(HUDToggleS2CPayload.class, HUDToggleS2CPayload.ID).registerS2C();
            addPacket(HUDPutElementS2CPayload.class, HUDPutElementS2CPayload.ID).registerS2C();
            addPacket(HUDDeleteElementS2CPayload.class, HUDDeleteElementS2CPayload.ID).registerS2C();
            addPacket(HUDClearS2CPayload.class, HUDClearS2CPayload.ID).registerS2C();
            addPacket(SetWindowTitleS2CPayload.class, SetWindowTitleS2CPayload.ID).registerS2C();
            addPacket(ServerCommandS2CPayload.class, ServerCommandS2CPayload.ID).registerS2C();
            addPacket(MoveToggleS2CPayload.class, MoveToggleS2CPayload.ID).registerS2C();
            addPacket(HUDSettingHotbarS2CPayload.class, HUDSettingHotbarS2CPayload.ID).registerS2C();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to add S2C packets", e);
        }
    }

    record Unbound<T>(Method method) implements Function<T, Object> {
        @Override
        @SuppressWarnings("unchecked")
        public T apply(Object o) {
            try {
                return (T) this.method.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to apply unbound", e);
            }
        }
    }

    private static final HashMap<String, Integer> TYPE_MAP = new HashMap<>(){{
        this.put(int.class.getName(), ILOAD);
        this.put(boolean.class.getName(), ILOAD);
        this.put(byte.class.getName(), ILOAD);
        this.put(short.class.getName(), ILOAD);
        this.put(char.class.getName(), ILOAD);

        this.put(long.class.getName(), LLOAD);
        this.put(float.class.getName(), FLOAD);
        this.put(double.class.getName(), DLOAD);
    }};

    @SuppressWarnings("unchecked")
    private <T extends Record> PayloadRecord<?> addPacket(Class<T> record, UIdentifier uid) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var id = Identifier.of(uid.namespace(), uid.path());
        var cpID = new CustomPayload.Id<>(id);

        PacketCodec<Object, ?> codec;

        var constructor = record.getDeclaredConstructors()[0];
        var types = constructor.getParameterTypes();

        var values = record.getRecordComponents();

        var wrapper = customPayload(cpID, values, types);
        var wrapperCo = wrapper.getDeclaredConstructor(types);

        if (values.length >= 1) {
            var valuesW = wrapper.getDeclaredMethods();

            var paramTypes = new ArrayList<Class<?>>();
            var paramValues = new ArrayList<>();
            for (var fComponent : valuesW) {
                if (fComponent.getName().equals("getId"))
                    continue;

                var fCodec = getPacketCodec(fComponent.getReturnType());
                var fFunction = new Unbound(fComponent);

                paramTypes.add(PacketCodec.class);
                paramTypes.add(Function.class);

                paramValues.add(fCodec);
                paramValues.add(fFunction);
            }

            passConstructor(wrapperCo, paramTypes, paramValues);

            var method = getMethod(paramTypes);
            var args = paramValues.toArray(Object[]::new);

            codec = (PacketCodec<Object, T>)
                    method.invoke(null, args);
        } else {
            // TODO: This doesn't require one argument - make an appropriate @SuppressWarnings annotation
            codec = PacketCodec.unit(wrapperCo.newInstance());
        }

        var pr = new PayloadRecord(cpID, codec, types);
        payloads.put(record.getName(), pr);
        return pr;
    }

    private @NotNull Class<?> customPayload(CustomPayload.Id<CustomPayload> cpID, RecordComponent[] values, Class<?>[] types) {
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
                    .intercept(new Implementation.Simple((mv, ctx, method) -> {
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
                    }));
        }

        return builder
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();
    }

    record PayloadRecord<T extends CustomPayload>(
            CustomPayload.Id<T> cpID,
            PacketCodec<RegistryByteBuf, T> codec,
            Class<?>[] types
    ) {
        public void registerS2C() {
            PayloadTypeRegistry.playS2C().register(cpID, codec);
        }
    }

    private static final HashMap<String, PayloadRecord<?>> payloads = new HashMap<>();
    private static final HashMap<Class<?>, PacketCodec<?, ?>> typeMap = new HashMap<>(){{
        this.put(String.class, PacketCodecs.STRING);
        this.put(short.class, PacketCodecs.SHORT);
        this.put(int.class, PacketCodecs.INTEGER);
        this.put(double.class, PacketCodecs.DOUBLE);
        this.put(boolean.class, PacketCodecs.BOOLEAN);
    }};

    private static @NotNull PacketCodec<?, ?> getPacketCodec(Class<?> type) {
        var fCodec = typeMap.get(type);
        if (fCodec == null)
            throw new RuntimeException("'" + type.getName() + "' type encountered; cannot get java type's codec");

        return fCodec;
    }

    private static @NotNull Method getMethod(ArrayList<Class<?>> paramTypes) {
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

    @FunctionalInterface
    interface VarFunction<T> {
        T apply(Object... varargs);
    }

    private static <T> void passConstructor(Constructor<T> constructor, ArrayList<Class<?>> paramTypes, ArrayList<Object> paramValues) {
        var params = constructor.getParameterCount();
        var callback = (VarFunction<T>) varargs -> {
            try {
                return constructor.newInstance(varargs);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to construct payload", e);
            }
        };

        // TODO: Reflectively shorten this code with a Map<int [parameterCount], ? super Function [callback]>
        if (params == 1) {
            paramTypes.add(Function.class);
            paramValues.add((Function<?, ?>) callback::apply);
        } else if (params == 2) {
            paramTypes.add(BiFunction.class);
            paramValues.add((BiFunction<?, ?, ?>) callback::apply);
        } else if (params == 3) {
            paramTypes.add(Function3.class);
            paramValues.add((Function3<?, ?, ?, ?>) callback::apply);
        } else if (params == 4) {
            paramTypes.add(Function4.class);
            paramValues.add((Function4<?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 5) {
            paramTypes.add(Function5.class);
            paramValues.add((Function5<?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 6) {
            paramTypes.add(Function6.class);
            paramValues.add((Function6<?, ?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 7) {
            paramTypes.add(Function7.class);
            paramValues.add((Function7<?, ?, ?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 8) {
            paramTypes.add(Function8.class);
            paramValues.add((Function8<?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 9) {
            paramTypes.add(Function9.class);
            paramValues.add((Function9<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 10) {
            paramTypes.add(Function10.class);
            paramValues.add((Function10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply);
        } else if (params == 11) {
            paramTypes.add(Function11.class);
            paramValues.add((Function11<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) callback::apply);
        }
    }

    private <T> void handleS2C(Class<T> packet, BiConsumer<T, ClientPlayNetworking.Context> handler) {
        try {
            var pr = payloads.get(packet.getName());
            var prCo = packet.getDeclaredConstructor(pr.types);
            var params = prCo.getParameters();

            ClientPlayNetworking.registerGlobalReceiver(pr.cpID, (payload, context) -> {
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

    @Override
    public void onInitializeClient() {
        handleS2C(ActivateS2CPayload.class, (payload, context) -> {
            NetworkUtil.enabled = true;
        });

        handleS2C(HUDToggleS2CPayload.class, (payload, context) -> {
            context.client().options.hudHidden = !payload.state();
        });

        handleS2C(HUDPutElementS2CPayload.class, (payload, context) -> {
            HudState.elements.put(payload.id(), new HudElement(
                    new Position(payload.x(), payload.y()),
                    payload.text()
            ));
        });

        handleS2C(HUDDeleteElementS2CPayload.class, (payload, context) -> {
            if (HudState.elements.remove(payload.id()) == null)
                logger.warn("Tried to delete non-existent HUD element");
        });

        handleS2C(HUDClearS2CPayload.class, (payload, context) -> {
            HudState.elements.clear();
        });

        handleS2C(SetWindowTitleS2CPayload.class, (payload, context) -> {
            WindowTitle.text = payload.title();
        });

        handleS2C(ServerCommandS2CPayload.class, (payload, context) -> {
            context.client().options.sneakKey.setPressed(payload.sneaking());
            context.client().options.sprintKey.setPressed(payload.sprinting());
        });

        handleS2C(MoveToggleS2CPayload.class, (payload, context) -> {
            MoveState.value = payload.canMove();
        });

        handleS2C(HUDSettingHotbarS2CPayload.class, (payload, context) -> {
            HudState.hotbar = payload.shown();
        });
    }
}
