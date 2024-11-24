package com.gtnewhorizons.CTF;

import static com.gtnewhorizons.CTF.utils.Structure.buildStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.ContainerItem;
import com.github.skjolber.packing.api.PackagerResult;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.packer.plain.PlainPackager;
import net.minecraft.server.MinecraftServer;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.tests.Test;
import com.gtnewhorizons.CTF.tests.TestSettings;
import com.gtnewhorizons.CTF.utils.JsonUtils;
import com.gtnewhorizons.CTF.utils.PrintUtils;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {

    private static boolean hasBuilt;

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
        if (!hasBuilt) {

            for (Test test : currentTests) {
                buildStructure(test);
            }

            hasBuilt = true;
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
                test.printAllMessages();
            }

            // Remove this set of tests, so we can move onto the next set, if there is one.
            testsMap.remove(currentSettings);

            // Reset so we can build more tests.
            hasBuilt = false;
        }
    }

    public static final HashMap<TestSettings, ArrayList<Test>> testsMap = new HashMap<>();

    public static void registerTests() {
        List<Test> tmpTestsStorage = new ArrayList<>();

        // Load tests from JSON and add them to the list
        for (JsonObject json : JsonUtils.loadAll()) {
            for (int i = 0; i < 2; i++) {
                Test test = new Test(json);
                testsMap.computeIfAbsent(test.getTestSettings(), k -> new ArrayList<>()).add(test);
                tmpTestsStorage.add(test);
            }
        }

        // Define container and packager setup
        Container container = Container.newBuilder()
            .withDescription("Total test area")
            .withSize(50, 256, 50)
            .withEmptyWeight(1)
            .withMaxLoadWeight(Integer.MAX_VALUE)
            .build();

        List<ContainerItem> containerItems = ContainerItem.newListBuilder()
            .withContainer(container)
            .build();

        PlainPackager packager = PlainPackager.newBuilder().build();

        // Convert tests to stackable items
        List<StackableItem> allTests = new ArrayList<>();
        for (Test test : tmpTestsStorage) {
            allTests.add(test.testBounds); // Assuming `testBounds` implements StackableItem
        }

        // Run the packager
        PackagerResult result = packager.newResultBuilder()
            .withContainers(containerItems)
            .withStackables(allTests)
            .build();

        if (result.isSuccess()) {
            Container sortedContainer = result.get(0);
            List<StackPlacement> placements = sortedContainer.getStack().getPlacements();

            // Create a map to link each test to its corresponding placement
            Map<StackableItem, StackPlacement> testToPlacementMap = new HashMap<>();

            // Assuming the items in `allTests` correspond one-to-one with placements in `sortedContainer`
            for (int i = 0; i < placements.size(); i++) {
                StackPlacement placement = placements.get(i);
                StackableItem stackableItem = allTests.get(i);

                // Add the mapping of the stackable item to the placement
                testToPlacementMap.put(stackableItem, placement);
            }

            // Now, map each test to its placement
            for (Test test : tmpTestsStorage) {
                StackPlacement placement = testToPlacementMap.get(test.testBounds);
                if (placement != null) {
                    // Link the test to the placement
                    test.setPlacement(placement); // Assuming `setPlacement` is a method in Test
                }
            }
        }
    }


}
