package com.myname.mymodid.procedures;

import com.myname.mymodid.Test;
import com.myname.mymodid.conditionals.registry.ConditionalFunction;
import com.myname.mymodid.conditionals.registry.RegisterConditionals;
import com.myname.mymodid.utils.PrintUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

public class CheckTile extends Procedure {
    public String funcID;
    public int x;
    public int y;
    public int z;

    public void handleEvent(Test test) {
        ConditionalFunction f = RegisterConditionals.getFunc(funcID);
        WorldServer worldServer = MinecraftServer.getServer().worldServers[0];
        TileEntity te = worldServer.getTileEntity(test.startX + x, test.startY + y, test.startZ + z);

        try {
            if (!f.checkCondition(te, worldServer)) {
                test.failed = true;

                if (optionalLabel != null) {
                    PrintUtils.printRed(optionalLabel + " FAILED");
                } else {
                    PrintUtils.printRed(" FAILED");
                }

            } else {
                PrintUtils.printGreen(optionalLabel + " PASSED");
            }
        } catch (Exception e) {
            test.failed = true;
            PrintUtils.printRed("Test " + optionalLabel + " threw exception, which was caught by CTF.");
            e.printStackTrace();
        }
    }
}
