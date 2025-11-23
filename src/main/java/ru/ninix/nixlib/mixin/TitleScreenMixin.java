package ru.ninix.nixlib.mixin;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Shadow
    @Nullable
    private SplashRenderer splash;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        System.out.println("NixLib Mixin - worked");

        this.splash = new SplashRenderer("NixLib Test");
    }
}
