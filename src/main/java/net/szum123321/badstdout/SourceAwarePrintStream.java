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

package net.szum123321.badstdout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.Optional;

public class SourceAwarePrintStream extends PrintStream {
    private final Logger outputLogger;
    private final String name;

    public SourceAwarePrintStream(@NotNull String name, @NotNull OutputStream out) {
        super(out);
        this.name = name;
        outputLogger = LogManager.getLogger(name);
    }

    @Override
    public void println(boolean x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(char x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(int x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(long x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(float x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(double x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(@NotNull char[] x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(@Nullable String x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void println(@Nullable Object x) {
        Optional<String> caller = getCallerClassName();
        outputLogger.info("[{}] {}", caller.orElse(name), x);
    }

    @Override
    public void print(boolean b) {
        this.println(b);
    }

    @Override
    public void print(char c) {
        this.println(c);
    }

    @Override
    public void print(int i) {
        this.println(i);
    }

    @Override
    public void print(long l) {
        this.println(l);
    }

    @Override
    public void print(float f) {
        this.println(f);
    }

    @Override
    public void print(double d) {
        this.println(d);
    }

    @Override
    public void print(@NotNull char[] s) {
        this.println(s);
    }

    @Override
    public void print(@Nullable String s) {
        this.println(s);
    }

    @Override
    public void print(@Nullable Object obj) {
        this.println(obj);
    }

    /*
     * There's no need to override printf, as default implementation literally just calls PrintStream.format(...)
     * And I strip trailing line breaks as the logger inserts one on its own
     */

    @Override
    public PrintStream format(@NotNull String format, Object... args) {
        Optional<String> caller = getCallerClassName();

        outputLogger.info("[{}] {}", caller.orElse(name), new Formatter().format(format, args).toString().stripTrailing());

        return this;
    }

    @Override
    public PrintStream format(Locale l, @NotNull String format, Object... args) {
        Optional<String> caller = getCallerClassName();

        outputLogger.info("[{}] {}", caller.orElse(name), new Formatter().format(l, format, args).toString().stripTrailing());

        return this;
    }

    //Find the class that called LoggerPrintStream.println
    private Optional<String> getCallerClassName() {
        Optional<StackWalker.StackFrame> optionalElement = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk((s) -> s
                        //skip all elements that are castable to PrintStream
                        .dropWhile(f -> PrintStream.class.isAssignableFrom(f.getDeclaringClass()))
                        .findFirst()
                );

        if(optionalElement.isEmpty()) return Optional.empty();

        StackWalker.StackFrame element = optionalElement.get();

        if(element.getClassName().startsWith("java") || element.getClassName().startsWith("sun")) {
            //Something form within jdk called System.out.println.
            //It is possible that it's due to some mod calling java built-in function, but it's safer to just let it go
            return Optional.empty();
        }

        /*
         * Minecraft internally uses a logger practically everywhere.
         * >Boys, we've got a lazy mixin on our hands<
         * Luckily mixin annotates every injected method with { @link org.spongepowered.asm.mixin.transformer.meta.MixinMerged }
         */
        if(element.getClassName().startsWith("net.minecraft")) {
            try {
                MixinMerged annotation = element.getDeclaringClass()
                        .getDeclaredMethod(element.getMethodName(), element.getMethodType().parameterArray())
                        .getDeclaredAnnotation(MixinMerged.class);

                //Sanity check
                if (annotation != null)
                    //Returns the name of mixin class
                    return Optional.of(annotation.mixin());

            } catch (NoSuchMethodException ignored) {
                //Shouldn't really ever happen
            }

            return Optional.empty();
        }

        return Optional.of(element.getClassName());
    }
}
