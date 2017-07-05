/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.machine.wayobjects.signals;

import mods.railcraft.api.core.RailcraftConstantsAPI;
import mods.railcraft.client.util.textures.TextureAtlasSheet;
import mods.railcraft.common.blocks.machine.BlockMachine;
import mods.railcraft.common.blocks.machine.IEnumMachine;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by CovertJaguar on 9/8/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class BlockMachineSignal<V extends Enum<V> & IEnumMachine<V>> extends BlockMachine<V> {
    @Deprecated
    public static final Set<IEnumMachine<?>> connectionsSenders = new HashSet<>();
    @Deprecated
    public static final Set<IEnumMachine<?>> connectionsListeners = new HashSet<>();
    @Deprecated
    public static final Set<IEnumMachine<?>> connectionsSelf = new HashSet<>();
    public static final PropertyEnum<EnumFacing> FRONT = PropertyEnum.create("front", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final PropertyBool CONNECTION_NORTH = PropertyBool.create("connection_north");
    public static final PropertyBool CONNECTION_SOUTH = PropertyBool.create("connection_south");
    public static final PropertyBool CONNECTION_EAST = PropertyBool.create("connection_east");
    public static final PropertyBool CONNECTION_WEST = PropertyBool.create("connection_west");
    public static final PropertyBool CONNECTION_DOWN = PropertyBool.create("connection_down");
    public static ResourceLocation[] lampTextures = new ResourceLocation[4];

    protected BlockMachineSignal() {
        super(false);
        setDefaultState(getDefaultState()
                .withProperty(FRONT, EnumFacing.NORTH)
                .withProperty(CONNECTION_NORTH, false)
                .withProperty(CONNECTION_SOUTH, false)
                .withProperty(CONNECTION_EAST, false)
                .withProperty(CONNECTION_WEST, false)
                .withProperty(CONNECTION_DOWN, true)
        );
        setCreativeTab(CreativeTabs.TRANSPORTATION);
        setHarvestLevel("crowbar", 0);
        setSoundType(SoundType.METAL);
        setResistance(50);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerTextures(TextureMap textureMap) {
        super.registerTextures(textureMap);
        lampTextures = TextureAtlasSheet.unstitchIcons(textureMap, new ResourceLocation(RailcraftConstantsAPI.MOD_ID, "signal_lamp_top"), new Tuple<>(4, 1));
    }

//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT_MIPPED;
//    }

    @Override
    public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
        return 8;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty(), FRONT, CONNECTION_DOWN, CONNECTION_NORTH, CONNECTION_SOUTH, CONNECTION_EAST, CONNECTION_WEST);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        Optional<TileSignalBase> tile = WorldPlugin.getTileEntity(worldIn, pos, TileSignalBase.class);
        tile.ifPresent(t -> t.getTileCache().resetTimers());
        state = state.withProperty(FRONT, tile.map(TileSignalBase::getFacing).orElse(EnumFacing.NORTH));
        state = state.withProperty(CONNECTION_DOWN, canConnectDown(state, worldIn, pos));
//        state = state.withProperty(CONNECTION_NORTH, tile.map(t -> t.isConnected(EnumFacing.NORTH)).orElse(false));
//        state = state.withProperty(CONNECTION_EAST, tile.map(t -> t.isConnected(EnumFacing.EAST)).orElse(false));
//        state = state.withProperty(CONNECTION_SOUTH, tile.map(t -> t.isConnected(EnumFacing.SOUTH)).orElse(false));
//        state = state.withProperty(CONNECTION_WEST, tile.map(t -> t.isConnected(EnumFacing.WEST)).orElse(false));
        return state;
    }

    private boolean canConnectDown(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (worldIn.isSideSolid(pos.down(), EnumFacing.UP, false))
            return true;
        BlockPos posDown = pos.down();
        IBlockState belowState = worldIn.getBlockState(posDown);
        return belowState.getBlock().canPlaceTorchOnTop(belowState, worldIn, pos);
    }

    @Override
    public ConnectStyle connectsToPost(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing face) {
        return ConnectStyle.TWO_THIN;
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.UP;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
