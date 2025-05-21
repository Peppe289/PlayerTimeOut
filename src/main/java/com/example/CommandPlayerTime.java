package com.example;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandPlayerTime {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("playtime")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    String name;

                    if (source.getPlayer() != null) {
                        name = source.getPlayer().getNameForScoreboard();
                    } else {
                        name = "";
                        source.sendFeedback(() -> Text.literal("This command can only be executed by a player."), false);
                        return 0; // Return 0 to indicate failure
                    }

                    source.sendFeedback(() -> feedBackTotalTimePlayer(name), false);
                    return 1;
                }).then(argument("player", string())
                        .requires(source -> source.hasPermissionLevel(0))
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> {
                                return feedBackTotalTimePlayer(getString(ctx, "player"));
                            }, false);
                            return 1;
                        }))
                .then(literal("set")
                        .requires(source -> source.hasPermissionLevel(0))
                        .then(argument("time", string())
                                .executes(ctx -> {
                                    String name = Objects.requireNonNull(ctx.getSource().getPlayer()).getNameForScoreboard();
                                    PlayerModel pm = PlayerTimeOutMod.getPlayerModels().get(name);

                                    String time = getString(ctx, "time");

                                    pm.setTimeOut(convertToLocalTime(time));
                                    return 1;
                                }))
                ));
    }

    private static LocalTime convertToLocalTime(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        int value;
        if (input.endsWith("m")) {
            value = Integer.parseInt(input.substring(0, input.length() - 1));
            return LocalTime.of(0, value);
        } else if (input.endsWith("h")) {
            value = Integer.parseInt(input.substring(0, input.length() - 1));
            return LocalTime.of(value, 0);
        } else {
            throw new IllegalArgumentException("Input must end with 'm' for minutes or 'h' for hours");
        }
    }

    private static Text feedBackTotalTimePlayer(String name) {
        PlayerModel pm = PlayerTimeOutMod.getPlayerModels().get(name);

        if (pm == null)
            return Text.literal("Player not found");

        System.out.println("PlayerModel: " + pm.getPlayerName());

        String totalTime = pm.getTotalTime().toString();
        LocalTime timeOut = pm.getTimeOut();
        if (timeOut == null) {
            return Text.literal("Total time of " + name + ": " + totalTime + "\n" + "Time out not set");
        }
        String timeOutStr = timeOut.toString();
        return Text.literal("Total time of " + name + ": " + totalTime + "\n" + "Time out: " + timeOutStr);
    }
}
