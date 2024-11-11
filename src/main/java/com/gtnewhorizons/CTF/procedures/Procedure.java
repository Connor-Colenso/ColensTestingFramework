package com.gtnewhorizons.CTF.procedures;

import com.gtnewhorizons.CTF.Test;

public abstract class Procedure {
    public int duration = 0;
    public String optionalLabel;

    public abstract void handleEvent(Test test);
}
