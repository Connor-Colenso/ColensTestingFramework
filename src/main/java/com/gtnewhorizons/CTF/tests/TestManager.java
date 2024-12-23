package com.gtnewhorizons.CTF.tests;

import static com.gtnewhorizons.CTF.MyMod.PRE_LOADED_TEST_JSONS;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.DUPLICATE_TEST;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.Structure.buildStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.gtnewhorizons.CTF.networking.CTFNetworkHandler;
import com.gtnewhorizons.CTF.networking.UpdateClientSideTestInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;

import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.plain.PlainPackager;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.MyMod;
import com.gtnewhorizons.CTF.utils.PrintUtils;

import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TestManager {

    private static final HashMap<Integer, AxisAlignedBB> dimensionIdToTestBounds = new HashMap<>();

    private static final int TEST_AREA_BUFFER_ZONE = 3;

    public static World getWorldByDimensionId(int dimensionId) {
        MinecraftServer server = MinecraftServer.getServer();
        return server.worldServerForDimension(dimensionId);
    }

    public static void loadAllTestChunks() {
        // Iterate over every dimension.
        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            World dimension = getWorldByDimensionId(dimensionId);
            if (dimension == null) {
                MyMod.CTF_LOG.error("Dimension with ID {} is not loaded.", dimensionId);
                continue;
            }

            AxisAlignedBB testBounds = dimensionIdToTestBounds.get(dimensionId);

            // Calculate chunk boundaries based on AxisAlignedBB.
            int chunkMinX = (int) Math.floor(testBounds.minX) >> 4;
            int chunkMaxX = (int) Math.floor(testBounds.maxX) >> 4;
            int chunkMinZ = (int) Math.floor(testBounds.minZ) >> 4;
            int chunkMaxZ = (int) Math.floor(testBounds.maxZ) >> 4;

            // Iterate over all relevant chunks and load them if they aren't loaded.
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(chunkX, chunkZ);

                    // Create or retrieve a Forge ticket
                    ForgeChunkManager.Ticket ticket = ForgeChunkManager
                        .requestTicket(MyMod.instance, dimension, ForgeChunkManager.Type.NORMAL);
                    if (ticket != null) {
                        ForgeChunkManager.forceChunk(ticket, chunkCoord);
                    } else {
                        throw new RuntimeException(
                            "Could not request chunk at " + chunkX + ", " + chunkZ + " to be loaded.");
                    }
                }
            }
        }
    }

    public static void expandTestZone(int dimensionId) {
        // Expand 1 chunk in each positive direction.
        dimensionIdToTestBounds.computeIfPresent(
            dimensionId,
            (k, testBounds) -> AxisAlignedBB.getBoundingBox(0, 0, 0, testBounds.maxX + 16, 255, testBounds.maxZ + 16));
    }

    public static void clearOutTestZones() {
        // Iterate over every dimension.
        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            World dimension = getWorldByDimensionId(dimensionId);

            // Retrieve the test bounds for this dimension.
            AxisAlignedBB testBounds = dimensionIdToTestBounds.get(dimensionId);

            // Calculate the block boundaries, including the buffer zone.
            int minX = (int) Math.floor(testBounds.minX) - TEST_AREA_BUFFER_ZONE;
            int maxX = (int) Math.floor(testBounds.maxX) + TEST_AREA_BUFFER_ZONE;
            int minZ = (int) Math.floor(testBounds.minZ) - TEST_AREA_BUFFER_ZONE;
            int maxZ = (int) Math.floor(testBounds.maxZ) + TEST_AREA_BUFFER_ZONE;

            // Iterate over all blocks within the calculated boundaries and set them to air.
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = 0; y < dimension.getHeight(); y++) { // 256 in vanilla 1.7.10
                        // Only replace non-air blocks, this seems wonky,
                        // but it actually stops us generating excess sections in chunks without much initialised.
                        if (!dimension.isAirBlock(x, y, z)) {
                            dimension.setBlock(x, y, z, Blocks.air);
                        }
                    }
                }
            }
        }
    }

    public static void registerDimensionalUsage(int dimensionId) {
        dimensionIdToTestBounds.putIfAbsent(dimensionId, AxisAlignedBB.getBoundingBox(0, 0, 0, 16, 255, 16));
    }

    public static HashMap<String, Test> uuidTestsMapping = new HashMap<>();
    public static final HashMap<TestSettings, ArrayList<Test>> testsMap = new HashMap<>();

    public static void registerTests() {

        // Convert tests to stackable items
        HashMap<Integer, List<StackableItem>> allTests = new HashMap<>();

        // Load tests from JSON and add them to the list
        for (JsonObject json : PRE_LOADED_TEST_JSONS) {
            int duplicates = 1;
            if (json.has(TEST_CONFIG)) {
                JsonObject testConfig = json.get(TEST_CONFIG)
                    .getAsJsonObject();
                if (testConfig.has(DUPLICATE_TEST)) {
                    duplicates = testConfig.get(DUPLICATE_TEST)
                        .getAsInt();
                }
            }

            for (int i = 0; i < duplicates; i++) {
                Test test = new Test(json);

                TestManager.registerDimensionalUsage(test.getDimensionID());
                addTest(test);

                allTests.computeIfAbsent(test.getDimensionID(), k -> new ArrayList<>())
                    .add(test.testBounds);
            }
        }

        long startTimeForTestPacking = System.currentTimeMillis(); // Capture start time, use this to measure how long
                                                                   // all this sorting took.

        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            // Define packager
            PlainPackager packager = PlainPackager.newBuilder()
                .build();
            boolean success;
            List<StackPlacement> placements = null;

            do {
                // Define container, this will expand itself to fit as many tests as needed.
                Container container = TestManager.getDimensionTestContainer(dimensionId);

                List<ContainerItem> containerItems = ContainerItem.newListBuilder()
                    .withContainer(container)
                    .build();

                // Run the packager
                PackagerResult result = packager.newResultBuilder()
                    .withContainers(containerItems)
                    .withStackables(allTests.get(dimensionId))
                    .build();

                success = result.isSuccess();

                if (success) {
                    placements = result.get(0)
                        .getStack()
                        .getPlacements();
                } else {
                    // Expand width and depth for the next iteration
                    expandTestZone(dimensionId);
                }

            } while (!success);

            // Process the successful result
            for (StackPlacement placement : placements) {
                Test test = uuidTestsMapping.get(
                    placement.getStackable()
                        .getId());
                test.setPlacement(placement);
            }
        }

        long duration = System.currentTimeMillis() - startTimeForTestPacking; // Calculate elapsed time

        MyMod.CTF_LOG.info("Execution time for CTF test packing: {} ms", duration);
    }

    private static Container getDimensionTestContainer(int dimensionId) {
        AxisAlignedBB testBounds = dimensionIdToTestBounds.get(dimensionId);
        int maxX = (int) testBounds.maxX;
        int maxZ = (int) testBounds.maxZ;

        return Container.newBuilder()
            .withDescription("Total test area")
            .withSize(maxX, 255, maxZ)
            .withEmptyWeight(1)
            .withMaxLoadWeight(Integer.MAX_VALUE)
            .build();
    }

    private static boolean firstTickOfTestSet;

    public static boolean AllTestsDone() {
        return testsMap.isEmpty();
    }

    public static void runTick(TickEvent.ServerTickEvent event) {

        if (testsMap.isEmpty()) return;

        TestSettings currentSettings = testsMap.keySet()
            .iterator()
            .next();
        ArrayList<Test> currentTests = testsMap.get(currentSettings);

        // Build the test, if we are in the first tick of their run time.
        if (!firstTickOfTestSet) {
            HashSet<Integer> dimensionIds = new HashSet<>();

            for (Test test : currentTests) {
                test.startTimer();
                buildStructure(test);

                dimensionIds.add(test.getDimensionID());
            }

            // Iterate over each dimension and
            for (int dimensionId : dimensionIds) {
                WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dimensionId);
                currentSettings.initGameRules(worldServer);
            }

            firstTickOfTestSet = true;
            return;
        }

        // Perform all necessary tasks in this tick for all tests.
        for (Test test : currentTests) {
            test.runProcedures();
        }

        // Check to see if all tests in the current set are done.
        boolean hasCompletedAllCurrentTests = true;
        for (Test test : currentTests) {
            hasCompletedAllCurrentTests &= test.isDone();
        }

        // Test done, throw away those tests. Move on.
        if (hasCompletedAllCurrentTests) {

            // Print all messages collected for each test.
            for (Test test : currentTests) {
                test.recordEndTime();
                test.printAllMessages();
                test.handleFinalConclusion();

                // When each test is done, update the client.
                for (EntityPlayerMP entityPlayerMP : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    CTFNetworkHandler.INSTANCE.sendTo(new UpdateClientSideTestInfo(test), entityPlayerMP);
                }
            }

            // Remove this set of tests, so we can move onto the next set, if there is one.
            testsMap.remove(currentSettings);

            if (testsMap.isEmpty()) {
                // We are done now, all tests are complete.
                PrintUtils.printSeparator();
                Test.printTotalTestsPassedInfo();
                PrintUtils.printSeparator();
            }

            // Reset so we can build more tests.
            firstTickOfTestSet = false;
        }
    }

    public static void addTest(Test test) {
        testsMap.computeIfAbsent(test.getTestSettings(), k -> new ArrayList<>())
            .add(test);
        uuidTestsMapping.put(test.getUUID(), test);
    }

    @SideOnly(Side.CLIENT)
    public static void addCustomTestFromUI(JsonObject currentTestInUI) {
        // Todo proper packet handling so we aren't just straight up violating server/client bounds.
        Test test = new Test(currentTestInUI);
        EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
        test.setManualPlacement(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);

        addTest(test);
    }
}
