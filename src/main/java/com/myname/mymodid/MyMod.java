package com.myname.mymodid;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = MyMod.MODID, version = Tags.VERSION, name = "MyMod", acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    public static final String MODID = "mymodid";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.myname.mymodid.ClientProxy", serverSide = "com.myname.mymodid.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        // Register the tick handler
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
    }
    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        jsons = loadAllJson();
    }

    public static List<JsonObject> jsons;

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }


    public List<JsonObject> loadAllJson() {
        List<JsonObject> jsonList = new ArrayList<>();
        String directoryPath = "CTF/";

        // Get the path to the directory where JSON files are located
        File directory = new File(Loader.instance().getConfigDir(), directoryPath);
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles == null) return null;

        for (File json : jsonFiles) {
            // Load each JSON file
            try (InputStream inputStream = Files.newInputStream(json.toPath());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
                jsonList.add(jsonObject); // Add the parsed JSON object to the list
            } catch (Exception e) {
                System.err.println("Error reading JSON file: " + json.getName());
                e.printStackTrace();
            }
        }

        return jsonList; // Return the list of JSON objects
    }

    // Registering server start event

    public static void autoLoadWorld() {

        // Specify the folder where the world should be located

        // Create a new world settings
        WorldSettings worldSettings = new WorldSettings(
            0, // Seed
            WorldSettings.GameType.CREATIVE, // Gamemode
            false, // Hardcore
            true, // Allow Cheats
            WorldType.FLAT // World type (e.g., flat, amplified)
        );

        Minecraft.getMinecraft().launchIntegratedServer("CTFWorld", "CTFWorld", worldSettings);


    }


}
