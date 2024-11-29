package com.gtnewhorizons.CTF;

import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.commands.CommandAddInstruction;
import com.gtnewhorizons.CTF.commands.CommandCompleteTest;
import com.gtnewhorizons.CTF.commands.CommandGetTileEntity;
import com.gtnewhorizons.CTF.commands.CommandInitTest;
import com.gtnewhorizons.CTF.commands.CommandLoadTest;
import com.gtnewhorizons.CTF.commands.CommandResetTest;
import com.gtnewhorizons.CTF.commands.instructions.AddFluidInstructions;
import com.gtnewhorizons.CTF.commands.instructions.AddItemInstructions;
import com.gtnewhorizons.CTF.commands.instructions.CheckTileInstructions;
import com.gtnewhorizons.CTF.commands.instructions.RegisterInstruction;
import com.gtnewhorizons.CTF.commands.instructions.RunTicksInstruction;
import com.gtnewhorizons.CTF.conditionals.TestConditional;
import com.gtnewhorizons.CTF.conditionals.registry.RegisterConditionals;
import com.gtnewhorizons.CTF.events.CTFWandEventHandler;
import com.gtnewhorizons.CTF.items.RegisterItems;
import com.gtnewhorizons.CTF.rendering.RenderCTFFrames;
import com.gtnewhorizons.CTF.rendering.RenderCTFRegionInfo;
import com.gtnewhorizons.CTF.tests.TestManager;
import com.gtnewhorizons.CTF.utils.JsonUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = MyMod.MODID,
    version = Tags.VERSION,
    name = "ColensTestingFramework",
    acceptedMinecraftVersions = "[1.7.10]")
public class MyMod {

    // The only reason I do this, is because I want to load the jsons into memory asap so we can detect errors early.
    public static final List<JsonObject> PRE_LOADED_TEST_JSONS = JsonUtils.loadAll();

    public static final String MODID = "CTF";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static MyMod instance;

    @SidedProxy(clientSide = "com.gtnewhorizons.CTF.ClientProxy", serverSide = "com.gtnewhorizons.CTF.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        new MovePlayer(); // Moves the user to specific x y z coords.
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForgeChunkManager.LoadingCallback() {

            @Override
            public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
                System.out.println("ticketsLoaded called.");
            }
        });

        // Register the tick handler
        FMLCommonHandler.instance()
            .bus()
            .register(new TickHandler());

        RegisterConditionals.conditionalRegister("ebfOutputCheck", TestConditional::isChestContainingStone);
        RegisterConditionals.conditionalRegister("func", TestConditional::isChestContainingStone);
        RegisterConditionals.conditionalRegister("checkStructure", TestConditional::checkStructure);

        RegisterInstruction.register("addItems", AddItemInstructions::add);
        RegisterInstruction.register("runTicks", RunTicksInstruction::add);
        RegisterInstruction.register("checkTile", CheckTileInstructions::add);
        RegisterInstruction.register("addFluids", AddFluidInstructions::add);

        if (event.getSide()
            .isClient()) {
            MinecraftForge.EVENT_BUS.register(new RenderCTFFrames());
            MinecraftForge.EVENT_BUS.register(new RenderCTFRegionInfo());
        }
        MinecraftForge.EVENT_BUS.register(new CTFWandEventHandler());

        RegisterItems.register();
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        TestManager.registerTests();
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
        event.registerServerCommand(new CommandGetTileEntity());
        event.registerServerCommand(new CommandLoadTest());
        event.registerServerCommand(new CommandInitTest());
        event.registerServerCommand(new CommandAddInstruction());
        event.registerServerCommand(new CommandCompleteTest());
        event.registerServerCommand(new CommandResetTest());
    }

}
