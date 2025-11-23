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
import net.minecraft.nbt.NbtCompound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import pl.olafcio.protocolextension.server.api.packetevents.ProtocolExtensionPacketEventsListener;
import pl.olafcio.protocolextension.server.api.virtual.API;
import pl.olafcio.protocolextension.server.api.packetevents.ProtocolExtensionPacketEventsAPI;
import pl.olafcio.protocolextension.both.Position;

import java.awt.*;
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

    private static API api = new ProtocolExtensionPacketEventsAPI();
    static {
        api.registerListener(new VariableAPI());
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
        PacketEvents.getAPI().getEventManager().registerListener(new ProtocolExtensionPacketEventsListener(), PacketListenerPriority.NORMAL);

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
                                api.forceHUD(player, state);
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
                                api.putHUD(player, id.shortValue(), (double) x, (double) y, textNBT);
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
                            api.deleteHUD(player, id.shortValue());
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
        //TODO: Make this implementation-specific
        var activation = ProtocolExtensionPacketEventsAPI.Packets.make("activation");
        PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer()).sendPacket(activation);
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

    public static API getAPI() {
        return api;
    }
}
