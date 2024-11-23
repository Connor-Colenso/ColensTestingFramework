package com.gtnewhorizons.CTF.procedures;

import static com.gtnewhorizons.CTF.utils.PrintUtils.GREEN;
import static com.gtnewhorizons.CTF.utils.PrintUtils.RED;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.conditionals.registry.ConditionalFunction;
import com.gtnewhorizons.CTF.conditionals.registry.RegisterConditionals;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.utils.PrintUtils;

public class CheckTile extends Procedure {

    public String funcID;
    public int x;
    public int y;
    public int z;

    public CheckTile(JsonObject instruction) {
        super();

        if (instruction.has("optionalLabel")) {
            optionalLabel = instruction.get("optionalLabel")
                .getAsString();
        }

        funcID = instruction.get("funcRegistry")
            .getAsString();
        x = instruction.get("x")
            .getAsInt();
        y = instruction.get("y")
            .getAsInt();
        z = instruction.get("z")
            .getAsInt();
    }

    public void handleEventCustom(Test test) {
        ConditionalFunction f = RegisterConditionals.getFunc(funcID);
        WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
        TileEntity te = worldServer.getTileEntity(test.startX + x, test.startY + y, test.startZ + z);

        try {
            if (!f.checkCondition(te, worldServer)) {
                test.failed = true;

                if (optionalLabel != null) {
                    PrintUtils.printColourConsole(RED, optionalLabel + " FAILED");
                } else {
                    PrintUtils.printColourConsole(RED, "FAILED");
                }

            } else {
                PrintUtils.printColourConsole(GREEN, optionalLabel + " PASSED");
            }
        } catch (Exception e) {
            test.failed = true;
            PrintUtils.printColourConsole(RED, "Test " + optionalLabel + " threw exception, which was caught by CTF.");
            e.printStackTrace();
        }
    }
}
