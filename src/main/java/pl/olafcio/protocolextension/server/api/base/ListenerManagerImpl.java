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

package pl.olafcio.protocolextension.server.api.base;

import org.bukkit.entity.Player;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionListener;
import pl.olafcio.protocolextension.server.api.virtual.managers.ListenerManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ListenerManagerImpl implements ListenerManager {
    private final ArrayList<ProtocolExtensionListener> listeners = new ArrayList<>();

    @Override
    public void registerListener(ProtocolExtensionListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean unregisterListener(ProtocolExtensionListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void dispatchEvent(String methodName, Player player, Class<?>[] types, Object[] values) {
        var typeList = new ArrayList<Class<?>>();
        typeList.add(Player.class);
        typeList.addAll(List.of(types));

        var valueList = new ArrayList<>();
        valueList.add(player);
        valueList.addAll(List.of(values));

        var typeArray = typeList.toArray(Class<?>[]::new);
        var valueArray = valueList.toArray(Object[]::new);

        for (var listener : listeners) {
            try {
                var method = listener.getClass().getDeclaredMethod(methodName, typeArray);
                method.invoke(listener, valueArray);
            } catch (NoSuchMethodException ignored) {
            } catch (RuntimeException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
