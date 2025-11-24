package ru.ninix.nixlib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.ninix.nixlib.client.util.NixRenderUtils;

import java.util.function.Supplier;

public class ShaderBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final Supplier<ShaderInstance> shaderSupplier;
    private final boolean seeThroughWalls;

    /**
     * @param context Context
     * @param shaderSupplier Function to get shader
     * @param seeThroughWalls "TRUE" = Wallhack visible / "FALSE" = Hidden behind walls
     */
    public ShaderBlockRenderer(BlockEntityRendererProvider.Context context, Supplier<ShaderInstance> shaderSupplier, boolean seeThroughWalls) {
        this.shaderSupplier = shaderSupplier;
        this.seeThroughWalls = seeThroughWalls;
    }

    @Override
    public void render(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ShaderInstance shader = shaderSupplier.get();
        if (shader == null) return;

        float time = (entity.getLevel().getGameTime() + partialTick) * 0.05f;

        poseStack.pushPose();

        float scale = 1.05f;
        float offset = (1.0f - scale) / 2.0f;
        poseStack.translate(offset, offset, offset);
        poseStack.scale(scale, scale, scale);

        NixRenderUtils.drawCubeWithShader(
            poseStack.last().pose(),
            shader,
            this.seeThroughWalls,
            (s) -> {
                if (s.getUniform("uTime") != null) {
                    s.getUniform("uTime").set(time);
                }
            }
        );

        poseStack.popPose();
    }
}
