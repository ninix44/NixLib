package ru.ninix.nixlib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.ninix.nixlib.api.IBedrockAnimatable;

public class UniversalBedrockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final ModelPart root;
    private final ResourceLocation texture;

    private float scale = 1.0f;
    private float offX = 0.5f, offY = 1.5f, offZ = 0.5f;
    private float rotX = 0f, rotY = 180f, rotZ = 0f;

    public UniversalBedrockRenderer(BlockEntityRendererProvider.Context context,
                                    ModelLayerLocation layer,
                                    ResourceLocation texture) {
        this.root = context.bakeLayer(layer);
        this.texture = texture;
    }

    public UniversalBedrockRenderer<T> setScale(float scale) {
        this.scale = scale;
        return this;
    }
    public UniversalBedrockRenderer<T> setOffset(float x, float y, float z) {
        this.offX = x; this.offY = y; this.offZ = z;
        return this;
    }
    public UniversalBedrockRenderer<T> setRotation(float x, float y, float z) {
        this.rotX = x; this.rotY = y; this.rotZ = z;
        return this;
    }

    @Override
    public void render(T entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (entity instanceof IBedrockAnimatable animatable) {
            root.resetPose();
            BedrockAnimationContext<T> ctx = new BedrockAnimationContext<>(entity, this.root, partialTick);
            animatable.animate(ctx);
        }

        poseStack.pushPose();

        poseStack.translate(offX, offY, offZ);

        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

        poseStack.scale(-scale, -scale, scale);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
