package com.gtnewhorizons.CTF.procedures;

import static com.gtnewhorizons.CTF.utils.PrintUtils.GREEN;
import static com.gtnewhorizons.CTF.utils.PrintUtils.RED;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.instructions.registry.ConditionalFunction;
import com.gtnewhorizons.CTF.instructions.registry.RegisterConditionals;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestManager;

public class CheckTile extends Procedure {

    public String funcID;
    public int x;
    public int y;
    public int z;

    public CheckTile(JsonObject instruction) {
        super(instruction);

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
        World dimension = TestManager.getWorldByDimensionId(test.getDimensionID());
        TileEntity te = dimension
            .getTileEntity(test.getStartStructureX() + x, test.getStartStructureY() + y, test.getStartStructureZ() + z);

        try {
            if (!f.checkCondition(te, dimension)) {
                test.didFail();

                if (optionalLabel != null) {
                    test.addMessage(RED, optionalLabel + " FAILED");
                } else {
                    test.addMessage(RED, "FAILED");
                }

            } else {
                test.addMessage(GREEN, optionalLabel + " PASSED");
            }
        } catch (Exception exception) {
            test.didFail();
            test.addMessage(RED, "Test " + optionalLabel + " threw exception, which was caught by CTF.");
            test.addMessage(RED, exception.toString());
        }
    }

    @Override
    public String toString() {
        return "Execute check " + funcID;
    }
}
