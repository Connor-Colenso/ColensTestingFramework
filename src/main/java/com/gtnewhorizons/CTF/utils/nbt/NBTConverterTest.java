package com.gtnewhorizons.CTF.utils.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.junit.jupiter.api.Test;

class NBTConverterTest {

    @Test
    public void testAllPrimitiveTypes() {
        // Test with all primitive NBT types in one compound
        NBTTagCompound compound = new NBTTagCompound();
        compound.setByte("byteValue", (byte) 1);
        compound.setShort("shortValue", (short) 100);
        compound.setInteger("intValue", 1000);
        compound.setLong("longValue", 10000L);
        compound.setFloat("floatValue", 1.23f);
        compound.setDouble("doubleValue", 4.56);

        String encoded = NBTConverter.encodeToString(compound);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(compound, decoded);
    }

    @Test
    public void testStringAndArrayTypes() {
        // Test with strings, byte arrays, and int arrays
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("stringValue", "Test String");
        compound.setTag("byteArray", new NBTTagByteArray(new byte[] { 1, 2, 3, 4, 5 }));
        compound.setTag("intArray", new NBTTagIntArray(new int[] { 10, 20, 30, 40, 50 }));

        String encoded = NBTConverter.encodeToString(compound);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(compound, decoded);
    }

    @Test
    public void testComplexNestedStructure() {
        // Complex nested structure with all types at various levels
        NBTTagCompound root = new NBTTagCompound();
        root.setString("rootString", "RootLevel");

        NBTTagCompound level1 = new NBTTagCompound();
        level1.setLong("level1Long", 123456789L);

        NBTTagCompound level2 = new NBTTagCompound();
        level2.setFloat("level2Float", 3.1415f);

        NBTTagCompound level3 = new NBTTagCompound();
        level3.setInteger("level3Int", 42);
        level3.setDouble("level3Double", 2.718);

        // Add level 3 to level 2, level 2 to level 1, level 1 to root
        level2.setTag("level3", level3);
        level1.setTag("level2", level2);
        root.setTag("level1", level1);

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }

    @Test
    public void testListOfCompounds() {
        // Test a list containing multiple compounds with mixed types
        NBTTagCompound root = new NBTTagCompound();
        root.setString("description", "List of Compounds");

        NBTTagList list = new NBTTagList();

        NBTTagCompound compound1 = new NBTTagCompound();
        compound1.setString("name", "Item1");
        compound1.setInteger("count", 5);

        NBTTagCompound compound2 = new NBTTagCompound();
        compound2.setString("name", "Item2");
        compound2.setDouble("weight", 2.5);

        list.appendTag(compound1);
        list.appendTag(compound2);

        root.setTag("items", list);

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }

    @Test
    public void testDeeplyNestedWithArrays() {
        // Deeply nested structure with byte and int arrays at different levels
        NBTTagCompound root = new NBTTagCompound();
        root.setString("description", "Deeply Nested Arrays");

        NBTTagCompound level1 = new NBTTagCompound();
        level1.setTag("byteArray", new NBTTagByteArray(new byte[] { 1, 2, 3 }));

        NBTTagCompound level2 = new NBTTagCompound();
        level2.setTag("intArray", new NBTTagIntArray(new int[] { 10, 20, 30 }));

        NBTTagCompound level3 = new NBTTagCompound();
        level3.setString("level3String", "DeepValue");

        // Nesting levels
        level2.setTag("level3", level3);
        level1.setTag("level2", level2);
        root.setTag("level1", level1);

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }

    @Test
    public void testLargeDataSet() {
        // Test with a large dataset of random values
        NBTTagCompound root = new NBTTagCompound();
        for (int i = 0; i < 100; i++) {
            NBTTagCompound subTag = new NBTTagCompound();
            subTag.setInteger("id", i);
            subTag.setString("name", "Item" + i);
            subTag.setDouble("value", Math.random());
            root.setTag("entry" + i, subTag);
        }

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }

    @Test
    public void testMixedTypeList() {
        // A list containing different NBT types (compounds, strings, integers)
        NBTTagCompound root = new NBTTagCompound();
        root.setString("description", "MixedTypeList");

        NBTTagList mixedList = new NBTTagList();

        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("key", "value");
        mixedList.appendTag(compound);

        mixedList.appendTag(new NBTTagString("stringElement"));
        mixedList.appendTag(new NBTTagInt(99));
        mixedList.appendTag(new NBTTagDouble(3.14159));

        root.setTag("mixedList", mixedList);

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }

    @Test
    public void testExtremelyDeeplyNested() {
        // Extremely deeply nested structure
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound current = root;
        for (int i = 0; i < 50; i++) {
            NBTTagCompound nextLevel = new NBTTagCompound();
            nextLevel.setInteger("level", i);
            current.setTag("nested", nextLevel);
            current = nextLevel;
        }

        String encoded = NBTConverter.encodeToString(root);
        NBTTagCompound decoded = NBTConverter.decodeFromString(encoded);

        assertEquals(root, decoded);
    }
}
