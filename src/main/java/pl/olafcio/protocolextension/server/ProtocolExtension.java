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

package pl.olafcio.protocolextension.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pl.olafcio.protocolextension.server.api.base.ListenerManagerImpl;
import pl.olafcio.protocolextension.server.api.base.ProtocolExtensionAPIRecord;
import pl.olafcio.protocolextension.server.api.player.packetevents.ProtocolExtensionPacketEventsPacketListener;
import pl.olafcio.protocolextension.server.api.player.packetevents.ProtocolExtensionPacketEventsPlayerManager;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionAPI;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.server.main.TCommands;
import pl.olafcio.protocolextension.server.main.TMultiversion;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class ProtocolExtension
        extends JavaPlugin
        implements Listener, TMultiversion, TCommands {

    // The fields here are not final incase another mod wants to inject into ProtocolExtension without mixins and
    // accessFlags reflection
    private static File configFile;
    private static FileConfiguration config;

    private static ProtocolExtensionAPI api = new ProtocolExtensionAPIRecord(
            new ListenerManagerImpl(),
            new ProtocolExtensionPacketEventsPlayerManager()
    );
    static {
        api.listenerManager().registerListener(new VariableAPI());
    }

    @Override
    public void onLoad() {
        configFile = new File(getDataFolder(), "config.yml");
        reloadConfig();
    }

    @Override
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        var defConfigStream = getResource("paper/config.yml");
        if (defConfigStream == null)
            return;

        config.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
        ));
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        PacketEvents.getAPI().getEventManager().registerListener(new ProtocolExtensionPacketEventsPacketListener(), PacketListenerPriority.NORMAL);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::registerCommands);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        api.listenerManager().dispatchEvent("onConnect", event, new Class<?>[]{}, new Object[]{});
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        api.listenerManager().dispatchEvent("onDisconnect", event, new Class<?>[]{}, new Object[]{});
    }

    public static ProtocolExtensionAPI getAPI() {
        return api;
    }

    @Override
    public ProtocolExtensionAPI api() {
        return api;
    }
}
