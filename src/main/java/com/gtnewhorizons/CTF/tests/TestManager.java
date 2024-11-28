package com.gtnewhorizons.CTF.tests;

import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.plain.PlainPackager;
import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.JsonUtils;
import com.gtnewhorizons.CTF.utils.PrintUtils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.DUPLICATE_TEST;
import static com.gtnewhorizons.CTF.utils.CommonTestFields.TEST_CONFIG;
import static com.gtnewhorizons.CTF.utils.Structure.buildStructure;

public class TestManager {

    private static final HashMap<Integer, AxisAlignedBB> dimensionIdToTestBounds = new HashMap<>();

    private static final int testAreaBufferZone = 3;

    private static final int chunkMinX = 0;
    private static final int chunkMinZ = 0;
    public static int chunkMaxX = 1;
    public static int chunkMaxZ = 1;

    public static World getWorldByDimensionId(int dimensionId) {
        MinecraftServer server = MinecraftServer.getServer();
        return server.worldServerForDimension(dimensionId);
    }

    public static void loadAllTestChunks() {

        // Iterate over every dimension.
        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            World dimension = getWorldByDimensionId(dimensionId);

            // Iterate over all relevant chunks, and load them.
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    if (!dimension.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                        // Force load the chunk if it's not already loaded.
                        dimension.getChunkProvider().loadChunk(chunkX, chunkZ);
                    }
                }
            }
        }
    }

    public static void expandTestZone(int dimensionId) {
        // Expand 1 chunk in each positive direction.
       dimensionIdToTestBounds.computeIfPresent(dimensionId, (k, testBounds) -> AxisAlignedBB.getBoundingBox(0, 0, 0, testBounds.maxX + 16, 255, testBounds.maxZ + 16));
    }

    public static void clearOutTestZones() {
        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            World dimension = getWorldByDimensionId(dimensionId);

            for (int x = -testAreaBufferZone; x < chunkMaxX * 16 + testAreaBufferZone; x++) {
                for (int z = -testAreaBufferZone; z < chunkMaxX * 16 + testAreaBufferZone; z++) {
                    for (int y = 0; y <= 256; y++) {
                        dimension.setBlock(x, y, z, Blocks.air);
                    }
                }
            }
        }
    }

    public static void registerDimensionalUsage(int dimensionId) {
        dimensionIdToTestBounds.putIfAbsent(dimensionId, AxisAlignedBB.getBoundingBox(0,0,0,15,255,15));
    }

    public static HashMap<String, Test> uuidTestsMapping = new HashMap<>();
    public static final HashMap<TestSettings, ArrayList<Test>> testsMap = new HashMap<>();

    public static void registerTests() {

        // Convert tests to stackable items
        HashMap<Integer, List<StackableItem>> allTests = new HashMap<>();

        // Load tests from JSON and add them to the list
        for (JsonObject json : JsonUtils.loadAll()) {
            int duplicates = 1;
            if (json.has(TEST_CONFIG)) {
                JsonObject testConfig = json.get(TEST_CONFIG).getAsJsonObject();
                if (testConfig.has(DUPLICATE_TEST)) {
                    duplicates = testConfig.get(DUPLICATE_TEST).getAsInt();
                }
            }

            for (int i = 0; i < duplicates; i++) {
                Test test = new Test(json);

                TestManager.registerDimensionalUsage(test.getDimension());

                testsMap.computeIfAbsent(test.getTestSettings(), k -> new ArrayList<>()).add(test);
                uuidTestsMapping.put(test.uuid, test);

                allTests.computeIfAbsent(test.getDimension(), k -> new ArrayList<>()).add(test.testBounds);
            }
        }

        long startTimeForTestPacking = System.currentTimeMillis(); // Capture start time, use this to measure how long all this sorting took.

        for (int dimensionId : dimensionIdToTestBounds.keySet()) {
            // Define packager
            PlainPackager packager = PlainPackager.newBuilder().build();
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
                    placements = result.get(0).getStack().getPlacements();
                } else {
                    // Expand width and depth for the next iteration
                    expandTestZone(dimensionId);
                }

            } while (!success);

            // Process the successful result
            for (StackPlacement placement : placements) {
                Test test = uuidTestsMapping.get(placement.getStackable().getId());
                test.setPlacement(placement);
            }
        }

        long duration = System.currentTimeMillis() - startTimeForTestPacking; // Calculate elapsed time

        System.out.println("Execution time for CTF test packing: " + duration + " ms");
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


    private static boolean firstTickOfTest;

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
        if (!firstTickOfTest) {

            for (Test test : currentTests) {
                test.startTimer();
                buildStructure(test);
            }

            firstTickOfTest = true;
            return;
        }

        // Perform all necessary tasks in this tick for all tests.
        for (Test test : currentTests) {
            test.runProcedures();
        }

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
            firstTickOfTest = false;
        }
    }
}
