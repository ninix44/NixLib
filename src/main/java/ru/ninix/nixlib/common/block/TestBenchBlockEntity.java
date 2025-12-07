package ru.ninix.nixlib.common.block;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.ninix.nixlib.NixLib;
import ru.ninix.nixlib.api.IBedrockAnimatable;
import ru.ninix.nixlib.client.renderer.BedrockAnimationContext;

public class TestBenchBlockEntity extends BlockEntity implements IBedrockAnimatable {

    public TestBenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(NixLib.TEST_BENCH_BE.get(), pos, blockState);
    }

    @Override
    public <T extends BlockEntity> void animate(BedrockAnimationContext<T> context) {

        float time = context.ageInTicks * 0.1f;

        ModelPart root = context.getBone("root");
        ModelPart rotor = context.getBone("rotor");

        if (root != null) {
            root.y = 24.0F + (float) Math.sin(time) * 5.0f;
            root.yRot = time * 0.5f;
        }

        if (rotor != null) {
            rotor.zRot = time * 10.0f;
        }
    }
}
