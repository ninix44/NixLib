package ru.ninix.nixlib.common.block;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.ninix.nixlib.NixLib;
import ru.ninix.nixlib.api.IBedrockAnimatable;
import ru.ninix.nixlib.client.renderer.BedrockAnimationContext;

public class BedrockBlockEntity extends BlockEntity implements IBedrockAnimatable {

    public BedrockBlockEntity(BlockPos pos, BlockState blockState) {
        super(NixLib.BEDROCK_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public <T extends BlockEntity> void animate(BedrockAnimationContext<T> context) {
        float time = context.ageInTicks * 0.05f;

        ModelPart root = context.getBone("root");

        root.yRot = time;
        root.y = 24.0F + (float) Math.sin(time) * 2.5f;
    }
}
