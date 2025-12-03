package ru.ninix.nixlib.client.vfx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VFXRenderer {
    private static final List<ActiveVFX> activeEffects = new ArrayList<>();

    public static class ActiveVFX {
        public final Vec3 pos;
        public final float yRot;
        public final float xRot;
        public final long duration;
        public final IVFXEffect effect;
        public final Color color1;
        public final Color color2;
        public final long startTime;

        public ActiveVFX(Vec3 pos, float yRot, float xRot, long duration, IVFXEffect effect, Color color1, Color color2) {
            this.pos = pos;
            this.yRot = yRot;
            this.xRot = xRot;
            this.duration = duration;
            this.effect = effect;
            this.color1 = color1;
            this.color2 = color2;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isAlive() {
            return (System.currentTimeMillis() - startTime) <= duration;
        }
    }

    public static void spawnEffect(Vec3 pos, float yRot, float xRot, IVFXEffect effect, long duration, Color color1, Color color2) {
        activeEffects.add(new ActiveVFX(pos, yRot, xRot, duration, effect, color1, color2));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || activeEffects.isEmpty()) return;

        long now = System.currentTimeMillis();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        Tesselator tessellator = Tesselator.getInstance();

        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Iterator<ActiveVFX> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            ActiveVFX vfx = iterator.next();

            if (!vfx.isAlive()) {
                iterator.remove();
                continue;
            }

            long elapsed = now - vfx.startTime;
            float progress = Math.min((float) elapsed / vfx.duration, 1.0f);

            poseStack.pushPose();
            poseStack.translate(vfx.pos.x - cameraPos.x, vfx.pos.y - cameraPos.y, vfx.pos.z - cameraPos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-vfx.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(vfx.xRot));

            vfx.effect.render(poseStack, bufferBuilder, progress, vfx.color1, vfx.color2);

            poseStack.popPose();
        }

        try {
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        } catch (Exception ignored) {}

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
