/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.aesthetics.glass;

import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.plugins.misc.MicroBlockPlugin;
import mods.railcraft.common.util.misc.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlockStrengthGlass extends BlockGlass {

    private static BlockStrengthGlass instance;
    public static boolean renderingHighlight;

    public static BlockStrengthGlass getBlock() {
        return instance;
    }

    public static void registerBlock() {
        if (instance == null)
            if (RailcraftConfig.isBlockEnabled("glass")) {
                instance = new BlockStrengthGlass(Railcraft.proxy.getRenderId());
                RailcraftRegistry.register(instance, ItemStrengthGlass.class);

                ForestryPlugin.addBackpackItem("builder", instance);

                for (int meta = 0; meta < 16; meta++) {
                    MicroBlockPlugin.addMicroBlockCandidate(instance, meta);
                }
            }
    }

    public static ItemStack getItem(int meta) {
        return getItem(1, meta);
    }

    public static ItemStack getItem(int qty, int meta) {
        if (instance == null) return null;
        return new ItemStack(instance, qty, meta);
    }

    private final int renderId;

    public BlockStrengthGlass(int renderId) {
        super(Material.glass, false);
        this.renderId = renderId;
        setResistance(5);
        setHardness(1);
        setStepSound(Block.soundTypeGlass);
        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
        setRegistryName("railcraft.glass");
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return meta;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    private enum Neighbors {

        TOP, BOTTOM
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int meta = 0; meta < 16; meta++) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
        int meta = world.getBlockMetadata(pos);
        if (meta != color) {
            world.setBlockState(pos, color, 3);
            return true;
        }
        return false;
    }

    @Override
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        if (renderingHighlight)
            return super.colorMultiplier(worldIn, pos, renderPass);
        int meta = worldIn.getBlockMetadata(pos);
        return EnumColor.fromId(15 - meta).getHexColor();
    }

}
