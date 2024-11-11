package com.gtnewhorizons.CTF.procedures;

import com.gtnewhorizons.CTF.Test;
import com.gtnewhorizons.CTF.utils.PrintUtils;
import net.minecraft.server.MinecraftServer;

public abstract class Procedure {
    public int duration = 0;
    public String optionalLabel;
    protected boolean shouldPrintInfo = true;

    public void handleEvent(Test test) {
        if (shouldPrintInfo) {
            int tickCounter = MinecraftServer.getServer().getTickCounter();
            if (optionalLabel != null) {
                PrintUtils.printColourConsole(PrintUtils.BLUE, "Procedure " + this.getClass().getSimpleName() + " with optionalLabel " + optionalLabel + " processed in tick " + tickCounter + " on the server thread.");
            } else {
                PrintUtils.printColourConsole(PrintUtils.BLUE,"Procedure " + this.getClass().getSimpleName() + " processed in tick " + tickCounter + " on the server thread.");
            }
        }

        handleEventCustom(test);
    };
    protected abstract void handleEventCustom(Test test);

}
