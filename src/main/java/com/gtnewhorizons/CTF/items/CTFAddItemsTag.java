package com.gtnewhorizons.CTF.items;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.IItemWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CTFAddItemsTag extends Item implements IItemWithModularUI {

    private static final String NBT_KEY_ITEMS = "SavedItems";

    public CTFAddItemsTag() {
        this.setUnlocalizedName("CTFAddItemsTag");
        this.setTextureName("minecraft:diamond");
        this.setMaxStackSize(1);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext, ItemStack heldStack) {
        // Create inventory handler for 9 slots
        ItemStackHandler inventoryHandler = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                saveInventoryToNBT(heldStack, this);
            }
        };

        // Load saved items into inventory slots from NBT
        loadInventoryFromNBT(heldStack, inventoryHandler);

        // Build the Modular UI Window
        ModularWindow.Builder builder = ModularWindow.builder(176, 166);

        // Add slot widgets for the inventory
        for (int i = 0; i < 9; i++) {
            int x = 8 + (i % 3) * 18;
            int y = 18 + (i / 3) * 18;

            BaseSlot baseSlot = new BaseSlot(inventoryHandler, i);
            SlotWidget slotWidget = new SlotWidget(baseSlot);
            slotWidget.setBackground(null, null);
            slotWidget.setPos(new Pos2d(x, y));
            builder.widget(slotWidget);
        }

        // Add a label as an example (optional)
        builder.widget(new TextWidget(StatCollector.translateToLocal("ui.ctf_add_items_tag.instruction"))
            .setPos(new Pos2d(8, 80)));

        // Bind the player's inventory
        builder.bindPlayerInventory(buildContext.getPlayer());

        return builder.build();
    }

    private void saveInventoryToNBT(ItemStack stack, ItemStackHandler handler) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = stack.getTagCompound();
        nbt.setTag(NBT_KEY_ITEMS, handler.serializeNBT());
    }

    private void loadInventoryFromNBT(ItemStack stack, ItemStackHandler handler) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_KEY_ITEMS)) {
            NBTTagCompound nbt = stack.getTagCompound().getCompoundTag(NBT_KEY_ITEMS);
            handler.deserializeNBT(nbt);
        }
    }


    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Open Modular UI for the player
            UIInfos.PLAYER_HELD_ITEM_UI.open(player, world, Vec3.createVectorHelper(100, 100, 100));
        }
        return stack;
    }
}
