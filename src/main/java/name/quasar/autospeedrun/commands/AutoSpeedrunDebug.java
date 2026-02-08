package name.quasar.autospeedrun.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import name.quasar.autospeedrun.AutoSpeedrunApi;
import name.quasar.autospeedrun.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static name.quasar.autospeedrun.AutoSpeedrun.userCode;

public class AutoSpeedrunDebug {
    private static HashMap<String, Runnable> debugOptions = null;
    private static HashMap<String, Consumer<Integer>> debugOptionsWithIntArg = null;

    public static class AutoSpeedrunDebugSuggestionsProvider implements SuggestionProvider<CommandSourceStack> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(
                CommandContext<CommandSourceStack> context,
                SuggestionsBuilder builder) {
            for (String debugOption : debugOptions.keySet()) {
                builder.suggest(debugOption);
            }
            for (String debugOption : debugOptionsWithIntArg.keySet()) {
                builder.suggest(debugOption);
            }
            return builder.buildFuture();
        }
    }

    public static void initDebugOptions() {
        if (debugOptions == null) {
            debugOptions = new HashMap<>();
            debugOptions.put("announcer_show", Util::enableAnnouncer);
            debugOptions.put("announcer_hide", Util::disableAnnouncer);
            debugOptions.put("screenshot", () -> AutoSpeedrunApi.screenshotAsync(1920, 1080));
        }
        if (debugOptionsWithIntArg == null) {
            debugOptionsWithIntArg = new HashMap<>();
            debugOptionsWithIntArg.put("tap_key", AutoSpeedrunApi::tapKey);
            debugOptionsWithIntArg.put("press_key", AutoSpeedrunApi::pressKey);
            debugOptionsWithIntArg.put("release_key", AutoSpeedrunApi::releaseKey);
            debugOptionsWithIntArg.put("mouse_move_x", (x) -> AutoSpeedrunApi.moveMouse(x, 0));
            debugOptionsWithIntArg.put("mouse_move_y", (y) -> AutoSpeedrunApi.moveMouse(0, y));
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        initDebugOptions();
        dispatcher.register(
            Commands.literal("autospeedrundebug").then(
                Commands.argument("subcommand", StringArgumentType.word())
                        .suggests(new AutoSpeedrunDebugSuggestionsProvider())
                        .executes(AutoSpeedrunDebug::execute).then(
                Commands.argument("intarg", IntegerArgumentType.integer(1, 9999)).executes(AutoSpeedrunDebug::execute))));
        dispatcher.register(
            Commands.literal("userdebug").then(
                Commands.argument("argument", StringArgumentType.greedyString())
                        .executes(AutoSpeedrunDebug::userExecute)
            )
        );
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        String command = StringArgumentType.getString(context, "subcommand");

        if (debugOptions.containsKey(command)) {
            debugOptions.get(command).run();
        } else if (debugOptionsWithIntArg.containsKey(command)) {
            int intarg = IntegerArgumentType.getInteger(context, "intarg");
            debugOptionsWithIntArg.get(command).accept(intarg);
        } else {
            context.getSource().sendFailure(new TextComponent("Unknown subcommand '" + command + "'"));
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int userExecute(CommandContext<CommandSourceStack> context) {
        String argument = StringArgumentType.getString(context, "argument");

        try {
            userCode.debug(argument);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
