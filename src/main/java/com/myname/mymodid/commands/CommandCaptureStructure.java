package com.myname.mymodid.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myname.mymodid.NBTConverter;
import com.myname.mymodid.Test;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.myname.mymodid.CommonTestFields.ENCODED_NBT;
import static com.myname.mymodid.CommonTestFields.INSTRUCTIONS;
import static com.myname.mymodid.CommonTestFields.STRUCTURE;
import static com.myname.mymodid.CommonTestFields.TEST_NAME;
import static com.myname.mymodid.TickHandler.addStructureInfo;
import static com.myname.mymodid.events.CTFWandEventHandler.firstPosition;
import static com.myname.mymodid.events.CTFWandEventHandler.secondPosition;
import static com.myname.mymodid.utils.Structure.captureStructureJson;

public class CommandCaptureStructure extends CommandBase {
    @Override
    public String getCommandName() {
        return "getStructure";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/getStructure";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        JsonObject structure = captureStructureJson();
        JsonObject overallJson = new JsonObject();
        overallJson.add(STRUCTURE, structure);
        overallJson.addProperty(TEST_NAME, "Blah blah");
        overallJson.add(INSTRUCTIONS, new JsonArray());
        saveJsonToFile(overallJson);

        Test testObj = new Test();
        addStructureInfo(overallJson, testObj);

        testObj.startX = 25;
        testObj.startY = 25;
        testObj.startZ = 25;

        testObj.buildStructure();
    }

    public static void saveJsonToFile(JsonObject overallJson) {
        // Create a pretty-printing Gson instance
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            // Define the file path in the CTF folder inside the config directory
            File ctfConfigDir = new File(Minecraft.getMinecraft().mcDataDir, "config/CTF/testing");
            if (!ctfConfigDir.exists()) {
                ctfConfigDir.mkdirs(); // Create CTF directory if it doesn't exist
            }

            // Specify the output file path within the CTF folder
            File outputFile = new File(ctfConfigDir, overallJson.get("testName").getAsString() + ".json");

            // Write the JSON content to the file with pretty printing
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(gson.toJson(overallJson));
                System.out.println("Json saved successfully to " + outputFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Error writing Json to file: " + e.getMessage());
        }
    }

}
