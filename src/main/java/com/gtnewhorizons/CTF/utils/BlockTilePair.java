package com.gtnewhorizons.CTF.utils;

import static com.gtnewhorizons.CTF.utils.CommonTestFields.ENCODED_NBT;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonObject;
import com.gtnewhorizons.CTF.utils.nbt.NBTConverter;

public class BlockTilePair {

    public Block block;
    public NBTTagCompound tile;
    public int meta;

    public BlockTilePair(String blockName, int meta, JsonObject jsonElement) {
        // Initialize the meta value
        this.meta = meta;

        // Initialize the Block using the blockName
        this.block = Block.getBlockFromName(blockName); // Assuming there's a method to get Block by name

        // Initialize the TileEntity if the jsonElement is not null
        if (jsonElement != null && !jsonElement.isJsonNull()) {

            // This will usually be encoded data, but if none then just make a blank tile entity and use that.
            String data = jsonElement.get(ENCODED_NBT)
                .getAsString();
            if (data.toLowerCase()
                .equals("default")) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString(
                    "id",
                    jsonElement.get("mappingForDefault")
                        .getAsString());
                this.tile = tag;
                return;
            }

            this.tile = NBTConverter.decodeFromString(data);
        } else {
            this.tile = null; // No TileEntity if jsonElement is null
        }
    }
}
