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

import net.minecraft.util.logging.LoggerPrintStream;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Optional;

@Mixin(LoggerPrintStream.class)
public class LoggerPrintStreamMixin {
    @Shadow @Final protected static Logger LOGGER;

    @Shadow @Final protected String name;

    //inject just before log function calls LOGGER.info and cancel from there
    @Inject(method = "log", at=@At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), cancellable = true)
    protected void log(String message, CallbackInfo ci) {
        if(name.equals("STDOUT")) {
            //Maybe let's not touch stderr
            Optional<String> callerClass = getCallerClassName();

            if (callerClass.isPresent()) {
                LOGGER.info("[{}] {}", callerClass.get(), message);
                ci.cancel();
            }
        }
    }

    //Find the class that called LoggerPrintStream.println
    private Optional<String> getCallerClassName() {
        StackWalker.StackFrame element = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk((s) -> s.skip(4).findFirst()) //get the 5th stack frame
                .orElse(null);

        if(element == null) return Optional.empty();

        if(element.getClassName().startsWith("java") || element.getClassName().startsWith("sun")) {
            //Something form within jdk called System.out.println.
            //It is possible that it's due to some mod calling java built-in function, but it's safer to just let it go
            return Optional.empty();
        }

        /*
         * Minecraft internally uses a logger everywhere.
         * Boys, we've got a lazy mixin no our hands
         * Luckily mixin annotates every injected method with { @link org.spongepowered.asm.mixin.transformer.meta.MixinMerged }
         */
        if(element.getClassName().startsWith("net.minecraft")) {
            try {
                //Get the caller class object itself
                Class<?> clazz = Class.forName(element.getClassName());

                //thanks to StackWalker we know the arguments that the mixed-in function takes, so we can get it using this:
                Method method = clazz.getDeclaredMethod(element.getMethodName(), element.getMethodType().parameterArray());

                MixinMerged annotation = method.getDeclaredAnnotation(MixinMerged.class);

                //Sanity check
                if (annotation != null)
                    //Returns the name of mixin class
                    return Optional.of(annotation.mixin());

            } catch (ClassNotFoundException | NoSuchElementException | NoSuchMethodException e) {
                //Shouldn't really ever happen
            }

            return Optional.empty();
        }

        return Optional.of(element.getClassName());
    }
}
