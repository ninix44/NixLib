package ru.ninix.nixlib.command;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ru.ninix.nixlib.cutscene.Cutscene;
import ru.ninix.nixlib.cutscene.CutsceneManager;
import ru.ninix.nixlib.network.CutsceneNetwork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CutsceneCommand {
    private static final Gson GSON = new Gson();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cutscene")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("start")
                .then(Commands.argument("name", StringArgumentType.string())
                    .suggests((c, b) -> suggestSavedCutscenes(c.getSource(), b))
                    .executes(context -> startCutscene(context.getSource(), StringArgumentType.getString(context, "name")))))
            .then(Commands.literal("save")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(context -> saveCutscene(context.getSource(), StringArgumentType.getString(context, "name"), null))))
            .then(Commands.literal("clear")
                .executes(context -> {
                    CutsceneManager.clearRecordingBuffer(context.getSource().getPlayerOrException());
                    context.getSource().sendSuccess(() -> Component.literal("Buffer cleared"), true);
                    return 1;
                }))
        );
    }

    private static int startCutscene(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        try {
            Cutscene cutscene = CutsceneManager.loadCutscene(player, name);
            if (cutscene == null || cutscene.keyframes.size() < 2) {
                source.sendFailure(Component.literal("Invalid or missing cutscene: " + name));
                return 0;
            }
            CutsceneNetwork.sendToPlayer(player, GSON.toJson(cutscene));
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int saveCutscene(CommandSourceStack source, String name, String entityName) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        try {
            if (CutsceneManager.saveRecording(player, name, entityName)) {
                source.sendSuccess(() -> Component.literal("Saved cutscene: " + name), true);
            } else {
                source.sendFailure(Component.literal("Buffer empty, use Camera Item first."));
            }
        } catch (IOException e) {
            source.sendFailure(Component.literal("Save error: " + e.getMessage()));
        }
        return 1;
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestSavedCutscenes(CommandSourceStack source, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        try {
            Path dir = source.getServer().getServerDirectory().resolve("cutscene");
            if (Files.exists(dir)) {
                try (Stream<Path> files = Files.list(dir)) {
                    return SharedSuggestionProvider.suggest(files.filter(p -> p.toString().endsWith(".json")).map(p -> p.getFileName().toString().replace(".json", "")), builder);
                }
            }
        } catch (Exception ignored) {}
        return com.mojang.brigadier.suggestion.Suggestions.empty();
    }
}
