package ru.ninix.nixlib.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import ru.ninix.nixlib.NixLib;

import javax.annotation.Nullable;
import java.io.IOException;

@net.neoforged.fml.common.EventBusSubscriber(modid = NixLib.MODID, value = Dist.CLIENT, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public class NixLibShaders {

    @Nullable
    private static ShaderInstance cosmicShader;

    @Nullable
    private static ShaderInstance constellationCardShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NixLib.MODID, "cosmic"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shaderInstance -> {
                    cosmicShader = shaderInstance;
                    NixLib.LOGGER.info("Cosmic Core Shader Loaded!");
                }
            );

            event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NixLib.MODID, "constellation_card"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shaderInstance -> {
                    constellationCardShader = shaderInstance;
                    NixLib.LOGGER.info("Constellation Card Shader Loaded!");
                }
            );
        } catch (IOException e) {
            NixLib.LOGGER.error("Failed to load shader", e);
        }
    }

    @Nullable
    public static ShaderInstance getCosmicShader() {
        return cosmicShader;
    }

    @Nullable
    public static ShaderInstance getConstellationCardShader() {
        return constellationCardShader;
    }
}
