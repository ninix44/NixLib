package ru.ninix.nixlib.client.renderer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashMap;
import java.util.Map;

public class BedrockAnimationContext<T extends BlockEntity> {
    public final T blockEntity;
    public final ModelPart root;
    public final float partialTick;
    public final float gameTime;
    public final float ageInTicks;

    private final Map<String, ModelPart> boneCache = new HashMap<>();

    public BedrockAnimationContext(T blockEntity, ModelPart root, float partialTick) {
        this.blockEntity = blockEntity;
        this.root = root;
        this.partialTick = partialTick;
        this.gameTime = (blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0) + partialTick;
        this.ageInTicks = this.gameTime;
    }

    public ModelPart getBone(String name) {
        if (boneCache.containsKey(name)) return boneCache.get(name);

        ModelPart found = findPart(root, name);
        if (found == null) found = createDummy();

        boneCache.put(name, found);
        return found;
    }

    private ModelPart findPart(ModelPart parent, String name) {
        if (parent.hasChild(name)) return parent.getChild(name);

        return null;
    }

    private static ModelPart createDummy() {
        return new ModelPart(java.util.Collections.emptyList(), java.util.Collections.emptyMap());
    }
}
