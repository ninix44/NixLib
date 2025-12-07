package ru.ninix.nixlib.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BedrockRenderUtils {

    public static void renderModel(
        PoseStack poseStack,
        ModelPart model,
        ResourceLocation texture,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        float x, float y, float z,
        float scaleX, float scaleY, float scaleZ,
        float rotX, float rotY, float rotZ
    ) {
        poseStack.pushPose();

        poseStack.translate(x, y, z);

        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

        poseStack.scale(scaleX, scaleY, scaleZ);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        model.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
