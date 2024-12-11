package com.gtnewhorizons.CTF.rendering;

import com.gtnewhorizons.CTF.tests.IClientSyncTestInfo;

import java.util.List;

public class RenderTest implements IClientSyncTestInfo {

    List<String> textToDraw;
    int bufferStartX;
    int bufferStartY;
    int bufferStartZ;
    int bufferEndX;
    int bufferEndY;
    int bufferEndZ;
    int dimension;
    boolean testFailed;
    String uuid;

    public RenderTest(List<String> textToDraw, int bufferStartX, int bufferStartY, int bufferStartZ, int bufferEndX, int bufferEndY, int bufferEndZ, int dimension, boolean testFailed, String uuid) {
        this.textToDraw = textToDraw;
        this.bufferStartX = bufferStartX;
        this.bufferStartY = bufferStartY;
        this.bufferStartZ = bufferStartZ;
        this.bufferEndX = bufferEndX;
        this.bufferEndY = bufferEndY;
        this.bufferEndZ = bufferEndZ;
        this.dimension = dimension;
        this.testFailed = testFailed;
        this.uuid = uuid;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int getBufferStartX() {
        return bufferStartX;
    }

    @Override
    public int getBufferStartY() {
        return bufferStartY;
    }

    @Override
    public int getBufferStartZ() {
        return bufferStartZ;
    }

    @Override
    public int getBufferEndX() {
        return bufferEndX;
    }

    @Override
    public int getBufferEndY() {
        return bufferEndY;
    }

    @Override
    public int getBufferEndZ() {
        return bufferEndZ;
    }

    @Override
    public List<String> relevantDebugInfo() {
        return textToDraw;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public boolean testFailed() {
        return testFailed;
    }
}
