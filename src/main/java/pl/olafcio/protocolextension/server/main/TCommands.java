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

package pl.olafcio.protocolextension.server.main;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pl.olafcio.protocolextension.both.Position;
import pl.olafcio.protocolextension.server.VariableAPI;
import pl.olafcio.protocolextension.server.api.virtual.ProtocolExtensionAPI;

import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@ApiStatus.NonExtendable
public interface TCommands extends TMultiversion {
    ProtocolExtensionAPI api();

    default Player getSinglePlayer(PlayerSelectorArgumentResolver arg, CommandSourceStack source) throws CommandSyntaxException {
        var playerList = arg.resolve(source);
        if (playerList.isEmpty()) {
            source.getSender().sendMessage("§8[§c🎈§8]§4 Error:§c Unknown player.");
            return null;
        }

        return playerList.getFirst();
    }

    @SuppressWarnings("UnstableApiUsage")
    default void registerCommands(ReloadableRegistrarEvent<@NotNull Commands> commands) {
        var px = Commands.literal("protocolextension")
                .requires(source -> source.getSender().hasPermission("protocolextension.command"))
                .executes(ctx -> {
                    var sender = ctx.getSource().getSender();
                    sender.sendMessage("§8[§c🎈§8]§7 This server is using §6ProtocolExtension§7 made by Olafcio with §c♥");
                    return SINGLE_SUCCESS;
                });

        px.then(registerHud());

        px.then(registerMousePos());
        px.then(registerSetWindowTitle());
        px.then(registerMoveToggle());

        // TODO: /px mouse-position watch <player>

        commands.registrar().register(px.build(), Set.of("px"));
    }

    private LiteralArgumentBuilder<CommandSourceStack> registerMoveToggle() {
        return Commands.literal("move-toggle")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.set_window_title"
                )))
                .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("state", BoolArgumentType.bool())
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var state = ctx.getArgument("state", boolean.class);

                    var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    var player = getSinglePlayer(playerArg, source);
                    if (player != null) {
                        api().playerManager().moveToggle(player, state);
                        source.getSender().sendMessage("§8[§c🎈§8]§7 Move toggle set to §n%s§7.".formatted(state));
                    }

                    return SINGLE_SUCCESS;
                })
        ));
    }

    private LiteralArgumentBuilder<CommandSourceStack> registerSetWindowTitle() {
        return Commands.literal("set-window-title")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.set_window_title"
                )))
                .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("text", ArgumentTypes.component())
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var component = ctx.getArgument("text", Component.class);

                    var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    var player = getSinglePlayer(playerArg, source);
                    if (player != null) {
                        api().playerManager().setWindowTitle(player, component);
                        source.getSender().sendMessage("§8[§c🎈§8]§7 Window title changed.");
                    }

                    return SINGLE_SUCCESS;
                })
        ));
    }

    default LiteralArgumentBuilder<CommandSourceStack> registerHud() {
        var hud = Commands.literal("hud");

        hud.then(Commands.literal("force")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.hud.force"
                )))
                .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("state", BoolArgumentType.bool())
                .executes(ctx -> {
                    var source = ctx.getSource();

                    var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    var state = ctx.getArgument("state", boolean.class);

                    var player = getSinglePlayer(playerArg, source);
                    if (player != null) {
                        api().playerManager().forceHUD(player, state);
                        source.getSender().sendMessage("§8[§c🎈§8]§7 Forced HUD state to §n" + state + "§7 for §6" + player.getName());
                    }

                    return SINGLE_SUCCESS;
                })
        )));

        hud.then(Commands.literal("put")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.hud.put"
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
                        api().playerManager().putHUD(player, id.shortValue(), (double) x, (double) y, textNBT);
                        source.getSender().sendMessage("§8[§c🎈§8]§7 HUD element added.");
                    }

                    return SINGLE_SUCCESS;
                })
        ))))));

        hud.then(Commands.literal("delete")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.hud.delete"
                )))
                .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("id", IntegerArgumentType.integer(0))
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var id = ctx.getArgument("id", int.class);

                    var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                    var player = getSinglePlayer(playerArg, source);
                    if (player != null) {
                        api().playerManager().deleteHUD(player, id.shortValue());
                        source.getSender().sendMessage("§8[§c🎈§8]§7 HUD element deleted.");
                    }

                    return SINGLE_SUCCESS;
                })
        )));

        hud.then(Commands.literal("clear")
                .requires(restricted(source -> source.getSender().hasPermission(
                        "protocolextension.command.hud.clear"
                )))
                .then(Commands.argument("player", ArgumentTypes.player())
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var playerArg = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);

                    var player = getSinglePlayer(playerArg, source);
                    if (player != null) {
                        api().playerManager().clearHUD(player);
                        source.getSender().sendMessage("§8[§c🎈§8]§7 HUD elements cleared.");
                    }

                    return SINGLE_SUCCESS;
                })
        ));

        return hud;
    }

    default LiteralArgumentBuilder<CommandSourceStack> registerMousePos() {
        return Commands.literal("mouse-position")
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
        ));
    }
}
