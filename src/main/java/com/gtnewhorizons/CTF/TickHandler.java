package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.utils.Structure.buildStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.plain.PlainPackager;
import com.gtnewhorizons.CTF.utils.PrintUtils;
import net.minecraft.server.MinecraftServer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestSettings;
import com.gtnewhorizons.CTF.utils.JsonUtils;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {

    private static boolean firstTickOfTest;

    public static boolean AllTestsDone() {
        return testsMap.isEmpty();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;
        if (MinecraftServer.getServer()
            .getTickCounter() == 1) return;

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

    public static final HashMap<TestSettings, ArrayList<Test>> testsMap = new HashMap<>();

    public static HashMap<String, Test> tmpTestsStorage = new HashMap<>();

    public static void registerTests() {

        // Convert tests to stackable items
        List<StackableItem> allTests = new ArrayList<>();

        // Load tests from JSON and add them to the list
        for (JsonObject json : JsonUtils.loadAll()) {
            for (int i = 0; i < 30; i++) {
                Test test = new Test(json);
                testsMap.computeIfAbsent(test.getTestSettings(), k -> new ArrayList<>()).add(test);
                tmpTestsStorage.put(test.uuid, test);
                allTests.add(test.testBounds);
            }
        }
        long startTimeForTestPacking = System.currentTimeMillis(); // Capture start time, use this to measure how long all this sorting took.

        // Initial container dimensions
        int initialWidth = 16;
        int initialHeight = 50;
        int initialDepth = 16;

        // Define packager
        PlainPackager packager = PlainPackager.newBuilder().build();
        boolean success;
        Container sortedContainer = null;

        do {
            // Define container with expanding size
            Container container = Container.newBuilder()
                .withDescription("Total test area")
                .withSize(initialWidth, initialHeight, initialDepth)
                .withEmptyWeight(1)
                .withMaxLoadWeight(Integer.MAX_VALUE)
                .build();

            List<ContainerItem> containerItems = ContainerItem.newListBuilder()
                .withContainer(container)
                .build();

            // Run the packager
            PackagerResult result = packager.newResultBuilder()
                .withContainers(containerItems)
                .withStackables(allTests)
                .build();

            success = result.isSuccess();

            if (success) {
                sortedContainer = result.get(0);
            } else {
                // Expand width and depth for the next iteration
                initialWidth += 16;
                initialDepth += 16;
            }

        } while (!success);

        // Process the successful result
        List<StackPlacement> placements = sortedContainer.getStack().getPlacements();
        for (StackPlacement placement : placements) {
            Test test = tmpTestsStorage.get(placement.getStackable().getId());
            test.setPlacement(placement);
        }

        long endTime = System.currentTimeMillis(); // Capture end time
        long duration = endTime - startTimeForTestPacking; // Calculate elapsed time

        System.out.println("Execution time for CTF test packing: " + duration + " ms");
    }
}
