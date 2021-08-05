package net.szum123321.test.badstdout;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BadstdoutTest implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("GENERIC TEST!");

        //Make sure we didn't break jdk
        new Exception("TEST EXCEPTION!").printStackTrace();
    }
}
