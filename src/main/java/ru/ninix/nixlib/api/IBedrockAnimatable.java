package ru.ninix.nixlib.api;

import net.minecraft.world.level.block.entity.BlockEntity;
import ru.ninix.nixlib.client.renderer.BedrockAnimationContext;

public interface IBedrockAnimatable {

    <T extends BlockEntity> void animate(BedrockAnimationContext<T> context);
}
