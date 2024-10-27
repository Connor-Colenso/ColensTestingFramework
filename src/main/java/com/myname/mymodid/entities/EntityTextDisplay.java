package com.myname.mymodid.entities;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTextDisplay extends EntityAnimal {

    public EntityTextDisplay(World world) {
        this(world, 25, 25, 25);
    }

    public EntityTextDisplay(World world, double x, double y, double z) {
        super(world);
        this.setSize(0.9F, 1.3F);
        this.setPosition(x, y, z);
        this.noClip = true; // Prevents collisions
        this.getNavigator().setAvoidsWater(true);

        // Add tasks
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.25D, Items.wheat, false));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));

        // Set the custom name tag
        this.setCustomNameTag("TESTING! 123");
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entity) {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setString("CustomName", this.getCustomNameTag());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);
        this.setCustomNameTag(tagCompound.getString("CustomName"));
    }

    @Override
    public String getCustomNameTag() {
        return "TESTING! 123"; // Return the custom name here
    }

    @Override
    public boolean getAlwaysRenderNameTag() {
        return true; // Always render the name tag
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20000000298023224D);
    }

    @Override
    protected String getLivingSound() {
        return "mob.cow.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.cow.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.cow.hurt";
    }

    @Override
    protected void func_145780_a(int x, int y, int z, Block blockIn) {
        this.playSound("mob.cow.step", 0.15F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected Item getDropItem() {
        return Items.leather;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        // You can define what items to drop here
    }
}
