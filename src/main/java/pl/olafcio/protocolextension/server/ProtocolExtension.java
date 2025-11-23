package pl.olafcio.protocolextension.server;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.olafcio.protocolextension.server.api.API;
import pl.olafcio.protocolextension.server.api.impl.PacketEventsAPI;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class ProtocolExtension extends JavaPlugin {
    private static File configFile;

    private static FileConfiguration config;
    private static API api = new PacketEventsAPI();

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
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            LiteralCommandNode<CommandSourceStack> px;
            commands.registrar().register(
                px = Commands.literal("protocolextension")
                    .requires(source -> source.getSender().hasPermission("protocolextension.command"))
                    .executes(ctx -> {
                        var sender = ctx.getSource().getSender();
                        sender.sendMessage("§8[§c🎈§8]§7 This server is using §6ProtocolExtension§7 made by Olafcio with §c♥");
                        return SINGLE_SUCCESS;
                    })
                    .then(Commands.literal("force-hud")
                            .requires(restricted(source -> source.getSender().hasPermission("protocolextension.command.force_hud")))
                            .then(Commands.argument("player", ArgumentTypes.player()).then(
                                    Commands.argument("state", BoolArgumentType.bool()).executes(ctx -> {
                                        var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                                        var state = ctx.getArgument("state", boolean.class);

                                        var player = getSinglePlayer(playerArg, ctx.getSource());
                                        if (player != null) {
                                            api.forceHUD(player, state);
                                            ctx.getSource().getSender().sendMessage("§8[§c🎈§8]§7 Forced HUD state to §l" + state + "§7 for §6" + player.getName());
                                        }

                                        return SINGLE_SUCCESS;
                                    })
                            ))
                    )
                .build()
            );

            commands.registrar().register(Commands.literal("px").redirect(px).build());
        });
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
