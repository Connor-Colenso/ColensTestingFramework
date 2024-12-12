package com.gtnewhorizons.CTF.tests;

import java.util.List;

public interface IClientSyncTestInfo {

    int getDimensionID();
    int getBufferStartX();
    int getBufferStartY();
    int getBufferStartZ();
    int getBufferEndX();
    int getBufferEndY();
    int getBufferEndZ();
    List<String> relevantDebugInfo();
    String getUUID();
    boolean testFailed();

}
