/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlockRailcraftFluid extends BlockFluidClassic {

    protected float particleRed;
    protected float particleGreen;
    protected float particleBlue;
    protected boolean flammable;
    protected int flammability = 0;

    public BlockRailcraftFluid(Fluid fluid, Material material) {
        super(fluid, material);
        setDensity(fluid.getDensity());
    }

    @Override
    public boolean canDrain(World world, BlockPos pos) {
        return true;
    }

    @Override
    public float getFilledPercentage(World world, BlockPos pos) {
        return 1;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos);
        if (flammable && world.provider.getDimension() == -1) {
            world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4F, true, true);
            world.setBlockToAir(pos);
        }
    }

    public BlockRailcraftFluid setFlammable(boolean flammable) {
        this.flammable = flammable;
        return this;
    }

    public BlockRailcraftFluid setFlammability(int flammability) {
        this.flammability = flammability;
        return this;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return flammable ? 300 : 0;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return flammability;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return flammable;
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return flammable && flammability == 0;
    }

    public BlockRailcraftFluid setParticleColor(float particleRed, float particleGreen, float particleBlue) {
        this.particleRed = particleRed;
        this.particleGreen = particleGreen;
        this.particleBlue = particleBlue;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        FluidTools.drip(world, pos, state, rand, particleRed, particleGreen, particleBlue);
    }

    static boolean isFluid(@Nonnull IBlockState blockstate) {
        return blockstate.getMaterial().isLiquid() || blockstate.getBlock() instanceof IFluidBlock;
    }

    // Railcraft: fix fluid level crash
    @Nonnull
    @Override
    public IBlockState getExtendedState(@Nonnull IBlockState oldState, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        IExtendedBlockState state = (IExtendedBlockState) oldState;
        state = state.withProperty(FLOW_DIRECTION, (float) getFlowDirection(world, pos));
        IBlockState[][] upBlockState = new IBlockState[3][3];
        float[][] height = new float[3][3];
        float[][] corner = new float[2][2];
        upBlockState[1][1] = world.getBlockState(pos.down(densityDir));
        height[1][1] = getFluidHeightForRender(world, pos, upBlockState[1][1]);
        if (height[1][1] == 1) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    corner[i][j] = 1;
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i != 1 || j != 1) {
                        upBlockState[i][j] = world.getBlockState(pos.add(i - 1, 0, j - 1).down(densityDir));
                        height[i][j] = getFluidHeightForRender(world, pos.add(i - 1, 0, j - 1), upBlockState[i][j]);
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    corner[i][j] = getFluidHeightAverage(height[i][j], height[i][j + 1], height[i + 1][j], height[i + 1][j + 1]);
                }
            }
            //check for downflow above corners
            boolean n = isFluid(upBlockState[0][1]);
            boolean s = isFluid(upBlockState[2][1]);
            boolean w = isFluid(upBlockState[1][0]);
            boolean e = isFluid(upBlockState[1][2]);
            boolean nw = isFluid(upBlockState[0][0]);
            boolean ne = isFluid(upBlockState[0][2]);
            boolean sw = isFluid(upBlockState[2][0]);
            boolean se = isFluid(upBlockState[2][2]);
            if (nw || n || w) {
                corner[0][0] = 1;
            }
            if (ne || n || e) {
                corner[0][1] = 1;
            }
            if (sw || s || w) {
                corner[1][0] = 1;
            }
            if (se || s || e) {
                corner[1][1] = 1;
            }
        }

        for (int i = 0; i < 4; i++) {
            EnumFacing side = EnumFacing.byHorizontalIndex(i);
            BlockPos offset = pos.offset(side);
            boolean useOverlay = world.getBlockState(offset).getBlockFaceShape(world, offset, side.getOpposite()) == BlockFaceShape.SOLID;
            state = state.withProperty(SIDE_OVERLAYS[i], useOverlay);
        }

        // Railcraft: fixed crash
        state = state.withProperty(LEVEL_CORNERS[0], Math.min(1, corner[0][0]));
        state = state.withProperty(LEVEL_CORNERS[1], Math.min(1, corner[0][1]));
        state = state.withProperty(LEVEL_CORNERS[2], Math.min(1, corner[1][1]));
        state = state.withProperty(LEVEL_CORNERS[3], Math.min(1, corner[1][0]));
        return state;
    }
}
