package com.gtnewhorizons.CTF.procedures;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.Test;

public class RunTicks extends Procedure {

    public RunTicks(JsonObject instruction) {
        shouldPrintInfo = false;

        duration = instruction.get("duration").getAsInt();
    }

    @Override
    public void handleEventCustom(Test test) {

    }
}
