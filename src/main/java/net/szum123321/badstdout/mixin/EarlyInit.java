/*
 * BadStdOut Copyright (C) 2021  Szum123321
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.szum123321.badstdout.mixin;

import net.minecraft.Bootstrap;
import net.szum123321.badstdout.SourceAwarePrintStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class EarlyInit {
    @Inject(method = "initialize", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/Bootstrap;setOutputStreams()V"))
    private static void setOutput(CallbackInfo ci) {
        //Minecraft itself replaces stdout with it's own implementation that redirects System.out.println to logger and adds '[STDOUT]'
        //this is the earliest it makes sense to replace the System.out
        System.setOut(new SourceAwarePrintStream("STDOUT", System.out));
    }
}
