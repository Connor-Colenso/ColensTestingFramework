package com.myname.mymodid.mixins.early;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "doesGuiPauseGame()Z", at = @At("RETURN"), cancellable = true)
    private void doesGuiPauseGame(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);  // Force the method to return false
    }
}
