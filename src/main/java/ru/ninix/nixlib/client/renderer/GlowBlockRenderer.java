package ru.ninix.nixlib.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.common.block.GlowBlockEntity;

public class GlowBlockRenderer implements BlockEntityRenderer<GlowBlockEntity> {

    public GlowBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GlowBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ShaderInstance shader = NixLibShaders.getRgbAuraShader();
        if (shader == null) return;

        float time = (entity.getLevel().getGameTime() + partialTick) * 0.05f;

        poseStack.pushPose();

        float scale = 1.05f;
        float offset = (1.0f - scale) / 2.0f;
        poseStack.translate(offset, offset, offset);
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        if (shader.getUniform("uTime") != null) {
            shader.getUniform("uTime").set(time);
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        drawCube(buffer, matrix);

        try {
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception ignored) {}

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private void drawCube(BufferBuilder builder, Matrix4f m) {
        builder.addVertex(m, 0, 1, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 1, 1).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 0).setUv(1, 0).setColor(255, 255, 255, 255);

        builder.addVertex(m, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 0, 0).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 0, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 0, 1).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(m, 0, 0, 1).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 1, 1).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(m, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 0).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 0, 0).setUv(1, 0).setColor(255, 255, 255, 255);

        builder.addVertex(m, 0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 0, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);

        builder.addVertex(m, 1, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 0).setUv(0, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 1, 1).setUv(1, 1).setColor(255, 255, 255, 255);
        builder.addVertex(m, 1, 0, 1).setUv(1, 0).setColor(255, 255, 255, 255);
    }
}
