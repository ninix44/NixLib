package ru.ninix.nixlib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.ninix.nixlib.client.util.BedrockRenderUtils;

public class BedrockBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    protected final ModelPart root;
    protected final ResourceLocation texture;

    public BedrockBlockRenderer(BlockEntityRendererProvider.Context context, ModelLayerLocation layer, ResourceLocation texture) {
        this.root = context.bakeLayer(layer);
        this.texture = texture;
    }

    @Override
    public void render(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        this.animate(entity, this.root, partialTick);

        BedrockRenderUtils.renderModel(
            poseStack,
            this.root,
            this.texture,
            bufferSource,
            packedLight,
            packedOverlay,
            0.5f, 1.5f, 0.5f,
            -1.0f, -1.0f, 1.0f,
            0, 0, 0
        );
    }

    protected void animate(T entity, ModelPart root, float partialTick) {
    }

    protected ModelPart getBone(String name) {
        if (this.root.hasChild(name)) {
            return this.root.getChild(name);
        }
        return null;
    }
}
