package com.gtnewhorizons.CTF.networking;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class CTFNetworkHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("CTF");

    public static void init() {
        INSTANCE.registerMessage(TransmitJsonForBuild.Handler.class, TransmitJsonForBuild.class, 0, Side.SERVER);
        INSTANCE.registerMessage(UpdateClientSideTestInfo.Handler.class, UpdateClientSideTestInfo.class, 1, Side.CLIENT);
    }
}
