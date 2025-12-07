package ru.ninix.nixlib.cutscene;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

public class CutsceneManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, List<Keyframe>> recordingBuffers = new HashMap<>();

    private static Path getCutscenesDirectory(ServerPlayer player) throws IOException {
        Path runDir = player.getServer().getServerDirectory();
        Path cutscenesDir = runDir.resolve("cutscene");
        if (!Files.exists(cutscenesDir)) Files.createDirectories(cutscenesDir);
        return cutscenesDir;
    }

    public static int addKeyframeToBuffer(ServerPlayer player, int durationTicks, float fov, float roll) {
        List<Keyframe> playerBuffer = recordingBuffers.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        Keyframe newKeyframe = new Keyframe(
            player.getX(), player.getEyeY(), player.getZ(),
            player.getXRot(), player.getYRot(), roll, fov, durationTicks, 0, 0
        );
        playerBuffer.add(newKeyframe);
        player.sendSystemMessage(Component.literal("Keyframe added: " + playerBuffer.size()), true);
        return playerBuffer.size();
    }

    public static boolean saveRecording(ServerPlayer player, String cutsceneName, String entityNameToAttach) throws IOException {
        List<Keyframe> playerBuffer = recordingBuffers.get(player.getUUID());
        if (playerBuffer == null || playerBuffer.isEmpty()) return false;

        Cutscene cutscene = new Cutscene();
        cutscene.keyframes = new ArrayList<>(playerBuffer);

        if (entityNameToAttach != null && !entityNameToAttach.isEmpty()) {
            cutscene.useEntity = true;
            cutscene.entityName = entityNameToAttach;
            // cutscene.lookAtEntity = false;

            Entity targetEntity = StreamSupport.stream(player.serverLevel().getAllEntities().spliterator(), false)
                .filter(e -> e.getName().getString().equalsIgnoreCase(entityNameToAttach)
                    || e.getEncodeId().equalsIgnoreCase(entityNameToAttach))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);

            if (targetEntity != null) {
                Vec3 entityPos = targetEntity.position();
                float eye = targetEntity.getEyeHeight();
                for(Keyframe kf : cutscene.keyframes) {
                    kf.offsetX = kf.x - entityPos.x;
                    kf.offsetY = kf.y - (entityPos.y + eye);
                    kf.offsetZ = kf.z - entityPos.z;
                    kf.x = 0; kf.y = 0; kf.z = 0;
                }
            } else {
                player.sendSystemMessage(Component.literal("Entity not found, saving absolute coords."));
                cutscene.useEntity = false;
            }
        } else {
            cutscene.useEntity = false;
            cutscene.entityName = "???";
            cutscene.lookAtEntity = false;
        }

        Path cutsceneFile = getCutscenesDirectory(player).resolve(cutsceneName + ".json");
        try (FileWriter writer = new FileWriter(cutsceneFile.toFile())) {
            GSON.toJson(cutscene, writer);
        }
        playerBuffer.clear();
        return true;
    }

    public static void clearRecordingBuffer(ServerPlayer player) {
        recordingBuffers.remove(player.getUUID());
    }

    public static Cutscene loadCutscene(ServerPlayer player, String cutsceneName) throws IOException {
        Path cutsceneFile = getCutscenesDirectory(player).resolve(cutsceneName + ".json");
        if (!Files.exists(cutsceneFile)) return null;
        try (FileReader reader = new FileReader(cutsceneFile.toFile())) {
            return GSON.fromJson(reader, Cutscene.class);
        }
    }
}
