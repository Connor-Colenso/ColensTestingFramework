package com.myname.mymodid.procedures;

import com.myname.mymodid.Test;
import com.myname.mymodid.conditionals.registry.ConditionalFunction;
import com.myname.mymodid.conditionals.registry.RegisterConditionals;
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
                    System.out.println("\u001B[31m" + optionalLabel + " FAILED\u001B[0m");
                } else {
                    System.out.println("\u001B[31mFAILED\u001B[0m");
                }

            } else {
                System.out.println("\u001B[32m" + optionalLabel + " PASSED\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mTest threw exception, which was caught by CTF.\u001B[0m");
        }
    }
}
