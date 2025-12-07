package ru.ninix.nixlib.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.ninix.nixlib.NixLib;

public class BedrockBlockEntity extends BlockEntity {
    public BedrockBlockEntity(BlockPos pos, BlockState blockState) {
        super(NixLib.BEDROCK_BLOCK_ENTITY.get(), pos, blockState);
    }
}
