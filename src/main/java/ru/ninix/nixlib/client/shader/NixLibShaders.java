package ru.ninix.nixlib.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import ru.ninix.nixlib.NixLib;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

@EventBusSubscriber(modid = NixLib.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NixLibShaders {

    @Nullable private static ShaderInstance cosmicShader;
    @Nullable private static ShaderInstance constellationCardShader;
    @Nullable private static ShaderInstance rainbowShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        register(event, "cosmic", s -> cosmicShader = s);
        register(event, "constellation_card", s -> constellationCardShader = s);
        register(event, "rainbow_shape", s -> rainbowShader = s);
    }

    private static void register(RegisterShadersEvent event, String path, Consumer<ShaderInstance> setter) {
        try {
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath(NixLib.MODID, path),
                    DefaultVertexFormat.POSITION_TEX
                ),
                setter
            );
            NixLib.LOGGER.info("Shader registered: " + path);
        } catch (IOException e) {
            NixLib.LOGGER.error("Failed to load shader: " + path, e);
        }
    }

    @Nullable public static ShaderInstance getCosmicShader() { return cosmicShader; }
    @Nullable public static ShaderInstance getConstellationCardShader() { return constellationCardShader; }
    @Nullable public static ShaderInstance getRainbowShader() { return rainbowShader; }
}
