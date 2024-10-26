package com.myname.mymodid;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myname.mymodid.commands.CommandCaptureStructure;
import com.myname.mymodid.commands.CommandGetTileEntity;
import com.myname.mymodid.conditionals.TestConditional;
import com.myname.mymodid.conditionals.registry.RegisterConditionals;
import com.myname.mymodid.events.CTFWandEventHandler;
import com.myname.mymodid.items.CTFWand;
import com.myname.mymodid.rendering.RenderCTFWandFrame;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.myname.mymodid.TickHandler.registerTests;

@Mod(modid = MyMod.MODID, version = Tags.VERSION, name = "MyMod", acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    public static final String MODID = "mymodid";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.myname.mymodid.ClientProxy", serverSide = "com.myname.mymodid.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        new MovePlayer(); // Moves the user to specific x y z coords.
    }


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register the tick handler
        FMLCommonHandler.instance().bus().register(new TickHandler());
        RegisterConditionals.conditionalRegister("testing", TestConditional::isChestContainingStone);
        GameRegistry.registerItem(new CTFWand(), "ctfWand");
        MinecraftForge.EVENT_BUS.register(new CTFWandEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderCTFWandFrame());
    }
    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        jsons = loadAllJson();
        registerTests();
    }

    public static List<JsonObject> jsons;
    public static List<Test> tests = new ArrayList<>();

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
        event.registerServerCommand(new CommandGetTileEntity());
        event.registerServerCommand(new CommandCaptureStructure());
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

                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
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
            false, // Map features (I think this is structures?)
            false, // Hardcore
            WorldType.FLAT // World type (e.g., flat, amplified)
        ).enableCommands();

        Minecraft.getMinecraft().launchIntegratedServer("CTFWorld1", "CTFWorld1", worldSettings);

    }


}
