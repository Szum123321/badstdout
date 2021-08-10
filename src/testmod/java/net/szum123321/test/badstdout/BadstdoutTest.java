package net.szum123321.test.badstdout;

import net.fabricmc.api.ModInitializer;

public class BadstdoutTest implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("PRINT TEST!");

        //God bless UTF-8
        System.out.printf("PRINTF test %s %.9f\n", "✅⧖⿈✊⊱⽃ⵛ⸥⾢⠢ⲁ₸⸌⚓\u200C␔♊⩯⁺⣭⢯⨸℅⚒", Math.PI);

        System.out.print(2137.69);

        System.out.println((Object)null);

        //Make sure we didn't break jdk
        new Exception("TEST EXCEPTION!").printStackTrace();
    }
}
