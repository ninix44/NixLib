package ru.ninix.nixlib.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.NixLib;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ShaderAPI {
    private static final Minecraft mc = Minecraft.getInstance();

    private static final List<ShaderBase> worldShaders = new CopyOnWriteArrayList<>();
    private static final List<ShaderBase> screenShaders = new CopyOnWriteArrayList<>();

    private static int lastWidth = 0;
    private static int lastHeight = 0;

    private ShaderAPI() {}

    public static void load(ShaderBase shader) {
        if (shader == null || isLoaded(shader.getShaderLocation())) return;
        shader.onEnable();

        if (shader.getPostChain() == null) return;

        if (shader.getRenderStage() == RenderStage.WORLD) {
            worldShaders.add(shader);
        } else {
            screenShaders.add(shader);
        }
        NixLib.LOGGER.info("Loaded Post-Shader: " + shader.getShaderLocation());
    }

    public static void unload(ResourceLocation location) {
        unloadFromList(worldShaders, location);
        unloadFromList(screenShaders, location);
    }

    private static void unloadFromList(List<ShaderBase> list, ResourceLocation loc) {
        list.removeIf(shader -> {
            if (shader.getShaderLocation().equals(loc)) {
                shader.onDisable();
                return true;
            }
            return false;
        });
    }

    public static void toggle(ShaderBase shader) {
        if (shader == null) return;
        if (isLoaded(shader.getShaderLocation())) {
            unload(shader.getShaderLocation());
        } else {
            load(shader);
        }
    }

    public static boolean isLoaded(ResourceLocation location) {
        return worldShaders.stream().anyMatch(s -> s.getShaderLocation().equals(location)) ||
            screenShaders.stream().anyMatch(s -> s.getShaderLocation().equals(location));
    }

    public static void tick() {
        if (mc.level == null) {
            if (!worldShaders.isEmpty()) {
                worldShaders.forEach(ShaderBase::onDisable);
                worldShaders.clear();
            }
            return;
        }

        if (mc.isPaused()) return;

        tickList(worldShaders);
        tickList(screenShaders);
    }

    private static void tickList(List<ShaderBase> list) {
        list.forEach(ShaderBase::onTick);
        list.removeIf(shader -> {
            if (shader.isFinished()) {
                shader.onDisable();
                return true;
            }
            return false;
        });
    }

    public static void renderWorldShaders(float partialTicks) {
        if (worldShaders.isEmpty()) return;
        handleResize();
        for (ShaderBase shader : worldShaders) {
            shader.process(partialTicks);
        }
    }


    private static void handleResize() {
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (lastWidth != width || lastHeight != height) {
            lastWidth = width;
            lastHeight = height;
            worldShaders.forEach(s -> s.resize(width, height));
            screenShaders.forEach(s -> s.resize(width, height));
        }
    }
}
