package com.gtnewhorizons.CTF;

import com.gtnewhorizons.CTF.ui.MainApp;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import javafx.application.Application;
import javafx.application.Platform;

import static javafx.application.Application.launch;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        Thread fxThread = new Thread(() -> {
            // Launch JavaFX application directly within the thread
            Application.launch(MainApp.class);
        });

        // Set the thread as daemon so it closes when the application stops
        fxThread.setDaemon(true);
        fxThread.start();
    }

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

}
