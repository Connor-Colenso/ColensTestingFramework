package com.gtnewhorizons.CTF.instructions.registry;

import java.util.HashMap;

public class RegisterConditionals {

    private static HashMap<String, ConditionalFunction> registry = new HashMap<>();

    public static void conditionalRegister(String funcID, ConditionalFunction func) {
        registry.put(funcID, func);
    }

    public static ConditionalFunction getFunc(String funcID) {
        return registry.get(funcID);
    }
}
