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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
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
            addS2CPacket(ActivateS2CPayload.class, ActivateS2CPayload.ID);
            addS2CPacket(HUDToggleS2CPayload.class, HUDToggleS2CPayload.ID);
            addS2CPacket(HUDPutElementS2CPayload.class, HUDPutElementS2CPayload.ID);
            addS2CPacket(HUDDeleteElementS2CPayload.class, HUDDeleteElementS2CPayload.ID);
            addS2CPacket(HUDClearS2CPayload.class, HUDClearS2CPayload.ID);
            addS2CPacket(SetWindowTitleS2CPayload.class, SetWindowTitleS2CPayload.ID);
            addS2CPacket(ServerCommandS2CPayload.class, ServerCommandS2CPayload.ID);
            addS2CPacket(MoveToggleS2CPayload.class, MoveToggleS2CPayload.ID);
            addS2CPacket(HUDSettingHotbarS2CPayload.class, HUDSettingHotbarS2CPayload.ID);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to add S2C packets", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Record> void addS2CPacket(Class<T> record, UIdentifier uid) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var id = Identifier.of(uid.namespace(), uid.path());
        PacketCodec<RegistryByteBuf, T> codec;

        var values = record.getRecordComponents();
        if (values.length >= 1) {
            var paramTypes = new ArrayList<Class<?>>();
            var paramValues = new ArrayList<>();
            for (var fComponent : values) {
                var fCodec = getPacketCodec(fComponent);
                var fFunction = MethodHandles.lookup().unreflect(fComponent);

                paramTypes.add(PacketCodec.class);
                paramTypes.add(Function.class);

                paramValues.add(fCodec);
                paramValues.add(fFunction);
            }

            paramTypes.add(Function.class);
            paramValues.add(record.getDeclaredConstructors()[0]);

            var method = getMethod(paramTypes);
            var args = paramValues.toArray(Object[]::new);

            codec = (PacketCodec<RegistryByteBuf, T>)
                    method.invoke(null, (Object[]) args);
        } else {
            codec = PacketCodec.unit(record.getDeclaredConstructor().newInstance());
        }

        var plays2c = PayloadTypeRegistry.playS2C();
        var register = plays2c.getClass().getDeclaredMethod("register", CustomPayload.Id.class, PacketCodec.class);

        var cpID = new CustomPayload.Id<>(id);
        packet2id.put(record, cpID);

        register.invoke(
                plays2c,
                cpID,
                codec
        );
    }

    private final HashMap<Class<? extends Record>, CustomPayload.Id<?>> packet2id = new HashMap<>();

    private static final HashMap<Class<?>, PacketCodec<?, ?>> typeMap = new HashMap<>(){{
        this.put(String.class, PacketCodecs.STRING);
        this.put(short.class, PacketCodecs.SHORT);
        this.put(int.class, PacketCodecs.INTEGER);
        this.put(double.class, PacketCodecs.DOUBLE);
        this.put(boolean.class, PacketCodecs.BOOLEAN);
    }};

    private static @NotNull PacketCodec<?, ?> getPacketCodec(Method fMethod) {
        var type = fMethod.getReturnType();

        var fCodec = typeMap.get(type);
        if (fCodec == null)
            throw new RuntimeException("'null' type encountered; cannot get java type's codec");

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
            throw new RuntimeException("Couldn't construct S2C packet: failed to retrieve appropriate tuple(...) variant");

        return method;
    }

    @SuppressWarnings("unchecked")
    private <T extends Record> void handleS2C(Class<T> packet, BiConsumer<T, ClientPlayNetworking.Context> handler) {
        var id = packet2id.get(packet);
        ClientPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
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
