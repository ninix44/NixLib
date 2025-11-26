package ru.ninix.nixlib.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.ninix.nixlib.NixLib;

public class TestBlockEntity extends BlockEntity {
    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(NixLib.TEST_BLOCK_ENTITY.get(), pos, blockState);
    }
}
