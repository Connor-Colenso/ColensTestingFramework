package com.gtnewhorizons.CTF.procedures;

import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;

import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.utils.PrintUtils;

public abstract class Procedure {

    public int duration = 0;
    public String optionalLabel;
    protected boolean shouldPrintInfo = true;
    protected JsonObject json;

    Procedure(JsonObject json) {
        this.json = json;
    }

    public void handleEvent(Test test) {
        if (shouldPrintInfo) {
            int tickCounter = MinecraftServer.getServer()
                .getTickCounter();
            if (optionalLabel != null) {
                test.addMessage(
                    PrintUtils.BLUE,
                    "Procedure " + this.getClass()
                        .getSimpleName()
                        + " with optionalLabel "
                        + optionalLabel
                        + " processed in tick "
                        + tickCounter
                        + " on the server thread.");
            } else {
                test.addMessage(
                    PrintUtils.BLUE,
                    "Procedure " + this.getClass()
                        .getSimpleName() + " processed in tick " + tickCounter + " on the server thread.");
            }
        }

        handleEventCustom(test);
    };

    protected abstract void handleEventCustom(Test test);

    public static Procedure buildProcedureFromJson(JsonObject instruction) {
        // Determine the type of procedure.
        String type = instruction.get("type")
            .getAsString();

        // Create the appropriate procedure based on the type.
        switch (type) {
            case "runTicks" -> {
                return new RunTicks(instruction);
            }
            case "checkTile" -> {
                return new CheckTile(instruction);
            }
            case "addItems" -> {
                return new AddItems(instruction);
            }
            case "addFluids" -> {
                return new AddFluids(instruction);
            }
        }

        return null;
    }

    public JsonObject getJson() {
        return json;
    }

    // This is how the procedure will render in the JavaFX UI ListView. Keep the text minimal!
    @Override
    public abstract String toString();
}
