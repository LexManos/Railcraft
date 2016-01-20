/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.signals;

import mods.railcraft.api.signals.SignalAspect;
import mods.railcraft.common.plugins.buildcraft.triggers.IAspectProvider;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.MiscTools;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.minecraftforge.common.util.EnumFacing.DOWN;
import static net.minecraftforge.common.util.EnumFacing.UP;

public abstract class TileSignalBase extends TileSignalFoundation implements ISignalTile, IAspectProvider {

    private static final EnumFacing[] UP_DOWN_AXES = new EnumFacing[]{UP, DOWN};
    protected static final float BOUNDS = 0.15f;
    private EnumFacing facing = EnumFacing.NORTH;
    private boolean prevLightState;
    private boolean prevBlinkState;

    @Override
    public boolean rotateBlock(EnumFacing axis) {
        if (axis == UP || axis == DOWN) {
            return false;
        }
        if (facing == axis) {
            facing = axis.getOpposite();
        } else {
            facing = axis;
        }
        markBlockForUpdate();
        return true;
    }

    @Override
    public EnumFacing[] getValidRotations() {
        return UP_DOWN_AXES;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int i, int j, int k) {
        getBlockType().setBlockBounds(BOUNDS, 0.35f, BOUNDS, 1 - BOUNDS, 1f, 1 - BOUNDS);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
        return AxisAlignedBB.getBoundingBox(i + BOUNDS, j + 0.35f, k + BOUNDS, i + 1 - BOUNDS, j + 1, k + 1 - BOUNDS);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
        return AxisAlignedBB.getBoundingBox(i + BOUNDS, j + 0.35f, k + BOUNDS, i + 1 - BOUNDS, j + 1, k + 1 - BOUNDS);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (Game.isNotHost(worldObj)) {
            updateLighting();
        }
    }

    private void updateLighting() {
        if (clock % Signals.LIGHT_CHECK_INTERVAL == 0) {
            boolean needsUpdate = false;
            boolean blinkState = SignalAspect.isBlinkOn();
            if (prevBlinkState != blinkState && isBlinking()) {
                prevBlinkState = blinkState;
                needsUpdate = true;
            }
            boolean lightState = isLit();
            if (prevLightState != lightState) {
                prevLightState = lightState;
                worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
                needsUpdate = true;
            }
            if (needsUpdate) {
                markBlockForUpdate();
            }
        }
    }

    protected boolean isLit() {
        return getSignalAspect().isLit();
    }

    protected boolean isBlinking() {
        return getSignalAspect().isBlinkAspect();
    }

    @Override
    public int getLightValue() {
        if (isLit()) {
            return 5;
        }
        return 0;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public void onBlockPlacedBy(EntityLivingBase entityliving, ItemStack stack) {
        super.onBlockPlacedBy(entityliving, stack);
        facing = MiscTools.getHorizontalSideClosestToPlayer(worldObj, xCoord, yCoord, zCoord, entityliving);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("Facing", (byte) facing.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        facing = EnumFacing.getOrientation(data.getByte("Facing"));
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);

        data.writeByte((byte) facing.ordinal());
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);

        facing = EnumFacing.getOrientation(data.readByte());

        markBlockForUpdate();
    }

    public abstract SignalAspect getSignalAspect();

    @Override
    public SignalAspect getTriggerAspect() {
        return getSignalAspect();
    }
}
