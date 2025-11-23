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

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class ProtocolExtension extends JavaPlugin implements Listener {
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

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var px = Commands.literal("protocolextension")
                        .requires(source -> source.getSender().hasPermission("protocolextension.command"))
                        .executes(ctx -> {
                            var sender = ctx.getSource().getSender();
                            sender.sendMessage("§8[§c🎈§8]§7 This server is using §6ProtocolExtension§7 made by Olafcio with §c♥");
                            return SINGLE_SUCCESS;
                        });

            px.then(Commands.literal("force-hud")
                        .requires(restricted(source -> source.getSender().hasPermission(
                                "protocolextension.command.force_hud"
                        )))
                        .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("state", BoolArgumentType.bool())
                        .executes(ctx -> {
                            var source = ctx.getSource();

                            var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            var state = ctx.getArgument("state", boolean.class);

                            var player = getSinglePlayer(playerArg, source);
                            if (player != null) {
                                api.playerManager().forceHUD(player, state);
                                source.getSender().sendMessage("§8[§c🎈§8]§7 Forced HUD state to §l" + state + "§7 for §6" + player.getName());
                            }

                            return SINGLE_SUCCESS;
                        })
            )));

            px.then(Commands.literal("mouse-position")
                        .requires(restricted(source -> source.getSender().hasPermission(
                                "protocolextension.command.mouse_position"
                        )))
                        .then(Commands.literal("get")
                        .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            var source = ctx.getSource();

                            var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            var player = getSinglePlayer(playerArg, source);

                            if (player != null) {
                                var pos = VariableAPI.getMousePos(player, Position.ZERO);
                                source.getSender().sendMessage("§8[§c🎈§8]§7 Player §6" + player.getName() + "§7's mouse is at §e" + pos.x() + "% " + pos.y() + "%");
                            }

                            return SINGLE_SUCCESS;
                        })
            )));

            px.then(Commands.literal("put-hud")
                        .requires(restricted(source -> source.getSender().hasPermission(
                                "protocolextension.command.put_hud"
                        )))
                        .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                        .then(Commands.argument("x", FloatArgumentType.floatArg(0, 1))
                        .then(Commands.argument("y", FloatArgumentType.floatArg(0, 1))
                        .then(Commands.argument("text", ArgumentTypes.component())
                        .executes(ctx -> {
                            var source = ctx.getSource();

                            var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                            var id = ctx.getArgument("id", int.class);
                            var x = ctx.getArgument("x", float.class);
                            var y = ctx.getArgument("y", float.class);
                            var textNBT = ctx.getArgument("text", Component.class);

                            var player = getSinglePlayer(playerArg, source);
                            if (player != null) {
                                api.playerManager().putHUD(player, id.shortValue(), (double) x, (double) y, textNBT);
                                source.getSender().sendMessage("§8[§c🎈§8]§7 HUD element added successfully.");
                            }

                            return SINGLE_SUCCESS;
                        })
            ))))));

            px.then(Commands.literal("delete-hud")
                    .requires(restricted(source -> source.getSender().hasPermission(
                            "protocolextension.command.delete_hud"
                    )))
                    .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("id", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        var source = ctx.getSource();
                        var id = ctx.getArgument("id", int.class);

                        var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                        var player = getSinglePlayer(playerArg, source);
                        if (player != null) {
                            api.playerManager().deleteHUD(player, id.shortValue());
                            source.getSender().sendMessage("§8[§c🎈§8]§7 HUD element deletion packet sent.");
                        }

                        return SINGLE_SUCCESS;
                    })
            )));

            // TODO: /px mouse-position watch <player>

            commands.registrar().register(px.build(), Set.of("px"));
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        api.playerManager().activate(event.getPlayer());
        api.listenerManager().dispatchEvent("onConnect", event, new Class<?>[]{}, new Object[]{});
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        api.listenerManager().dispatchEvent("onDisconnect", event, new Class<?>[]{}, new Object[]{});
    }

    private Player getSinglePlayer(PlayerSelectorArgumentResolver arg, CommandSourceStack source) throws CommandSyntaxException {
        var playerList = arg.resolve(source);
        if (playerList.isEmpty()) {
            source.getSender().sendMessage("§8[§c🎈§8]§4 Error:§c Unknown player.");
            return null;
        }

        return playerList.getFirst();
    }

    /**
     * Mango boy multi-version compatibility
     */
    private Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate) {
        try {
            return Commands.restricted(predicate);
        } catch (NoSuchMethodError e) {
            return predicate;
        }
    }

    public static ProtocolExtensionAPI getAPI() {
        return api;
    }
}
