package ru.ninix.nixlib.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.ninix.nixlib.client.util.CameraStateManager;
import ru.ninix.nixlib.network.CutsceneNetwork;

public class CameraItem extends Item {
    public CameraItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            float fov = CameraStateManager.getFov();
            float roll = CameraStateManager.getRoll();
            CutsceneNetwork.sendToServer(40, fov, roll);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
