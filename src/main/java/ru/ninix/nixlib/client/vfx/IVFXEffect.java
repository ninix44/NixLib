package ru.ninix.nixlib.client.vfx;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;

@FunctionalInterface
public interface IVFXEffect {
    void render(PoseStack poseStack, BufferBuilder bufferBuilder, float progress, Color color1, Color color2);
}
