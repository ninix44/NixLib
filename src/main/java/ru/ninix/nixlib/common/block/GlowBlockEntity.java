package ru.ninix.nixlib.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.ninix.nixlib.NixLib;

public class GlowBlockEntity extends BlockEntity {
    public GlowBlockEntity(BlockPos pos, BlockState blockState) {
        super(NixLib.EXAMPLE_BLOCK_ENTITY.get(), pos, blockState);
    }
}
