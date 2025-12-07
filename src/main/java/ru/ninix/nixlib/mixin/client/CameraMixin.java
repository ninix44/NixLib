package ru.ninix.nixlib.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ninix.nixlib.client.cutscene.ClientCutsceneManager;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Vec3 position;

    @Inject(method = "setup", at = @At("RETURN"))
    private void nixlib_overrideCameraPosition(BlockGetter level, Entity entity, boolean detached, boolean thirdPerson, float partialTick, CallbackInfo ci) {
        if (ClientCutsceneManager.isPlaying()) {
            ClientCutsceneManager.updateValuesFromMixin(partialTick);

            this.position = new Vec3(
                ClientCutsceneManager.cameraX,
                ClientCutsceneManager.cameraY,
                ClientCutsceneManager.cameraZ
            );
        }
    }
}
