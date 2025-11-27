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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    @SuppressWarnings("unchecked")
    private <T extends Record> PayloadRecord<?> addPacket(Class<T> record, UIdentifier uid) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        PacketCodec<RegistryByteBuf, T> codec;

        var values = record.getRecordComponents();
        if (values.length >= 1) {
            var paramTypes = new ArrayList<Class<?>>();
            var paramValues = new ArrayList<>();
            for (var fComponent : values) {
                var fCodec = getPacketCodec(fComponent.getType());
                var fFunction = new Unbound(fComponent.getAccessor());

                paramTypes.add(PacketCodec.class);
                paramTypes.add(Function.class);

                paramValues.add(fCodec);
                paramValues.add(fFunction);
            }

            passConstructor(record.getDeclaredConstructors()[0], paramTypes, paramValues);

            var method = getMethod(paramTypes);
            var args = paramValues.toArray(Object[]::new);

            codec = (PacketCodec<RegistryByteBuf, T>)
                    method.invoke(null, args);
        } else {
            codec = PacketCodec.unit(record.getDeclaredConstructor().newInstance());
        }

        var id = Identifier.of(uid.namespace(), uid.path());
        var cpID = new CustomPayload.Id<>(id);

        var pr = new PayloadRecord(cpID, codec);
        payloads.put(record.getName(), pr);
        return pr;
    }

    record PayloadRecord<T extends CustomPayload>(
            CustomPayload.Id<T> cpID,
            PacketCodec<RegistryByteBuf, T> codec
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

    @SuppressWarnings("unchecked")
    private <T extends Record> void handleS2C(Class<T> packet, BiConsumer<T, ClientPlayNetworking.Context> handler) {
        var pr = payloads.get(packet.getName());
        ClientPlayNetworking.registerGlobalReceiver(pr.cpID, (payload, context) -> {
            context.client().execute(() -> handler.accept((T) payload, context));
        });
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
