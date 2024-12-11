package com.gtnewhorizons.CTF;

import com.gtnewhorizons.CTF.events.ServerSideUpdateClientsWithTestsOnJoin;
import javafx.application.Application;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import com.gtnewhorizons.CTF.ui.MainApp;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        processClientsideConfig(event);

        Thread fxThread = new Thread(() -> {
            // Launch JavaFX application directly within the thread
            Application.launch(MainApp.class);
        });

        // Set the thread as daemon so it closes when the application stops
        fxThread.setDaemon(true);
        fxThread.start();
    }

    public static boolean enableTestBoundsCollisionVisualisation;

    private void processClientsideConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        // Load the configuration
        config.load();

        // Define a client-only setting
        enableTestBoundsCollisionVisualisation = config.get(
            "Debug",
            "enableTestBoundsCollisionVisualisation",
            false,
            "This is a very laggy feature when used with lots of tests as it checks if every test intersects every other test and renders them with red bounds if they do, so keep this default off unless you are specifically trying to diagnose an issue with CTF.")
            .getBoolean();
    }

}
