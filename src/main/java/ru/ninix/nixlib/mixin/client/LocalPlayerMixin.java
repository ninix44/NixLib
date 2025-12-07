package ru.ninix.nixlib.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ninix.nixlib.client.cutscene.ClientCutsceneManager;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void nixlib_onAiStep(CallbackInfo ci) {
        if (ClientCutsceneManager.isPlaying()) {
            this.noPhysics = true;
            this.setOnGround(false);
            this.fallDistance = 0;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }
}
