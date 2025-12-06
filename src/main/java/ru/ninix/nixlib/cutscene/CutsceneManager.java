package ru.ninix.nixlib.cutscene;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

    public static boolean saveRecording(ServerPlayer player, String cutsceneName, String entityName) throws IOException {
        List<Keyframe> playerBuffer = recordingBuffers.get(player.getUUID());
        if (playerBuffer == null || playerBuffer.isEmpty()) return false;

        Cutscene cutscene = new Cutscene();
        cutscene.keyframes = new ArrayList<>(playerBuffer);

        if (entityName != null && !entityName.isEmpty()) {
            cutscene.useEntity = true;
            cutscene.entityName = entityName;
            cutscene.lookAtEntity = false;

            Keyframe firstFrame = cutscene.keyframes.get(0);
            double originX = firstFrame.x;
            double originY = firstFrame.y;
            double originZ = firstFrame.z;

            for(Keyframe kf : cutscene.keyframes) {
                kf.offsetX = kf.x - originX;
                kf.offsetY = kf.y - originY;
                kf.offsetZ = kf.z - originZ;
                kf.x = 0; kf.y = 0; kf.z = 0;
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
