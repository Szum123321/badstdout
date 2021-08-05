package net.szum123321.test.badstdout.mixin;

import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class TestMixin {
    @Inject(method = "initialize", at = @At("RETURN"))
    private static void testVoid(CallbackInfo ci) {
        System.out.println("MIXIN TEST!");
    }
}
