//FlamePaper - Push-based-hoppers
package org.github.paperspigot;

import net.minecraft.server.Block;
import net.minecraft.server.BlockChest;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.IInventory;
import net.minecraft.server.TileEntity;
import net.minecraft.server.TileEntityHopper;
import net.minecraft.server.World;

public class HopperHelper {
    //Taco - Add-a-flag-to-disable-InventoryMoveItemEvent
    public static boolean isFireInventoryMoveItemEvent(net.minecraft.server.IHopper hopper) {
        return top.speedcubing.paper.CubingPaperConfig.isHopperFireIMIE && org.bukkit.event.inventory.InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length > 0;
    }

    public static TileEntityHopper getHopper(World world, BlockPosition pos) {
        if (world.getType(pos).getBlock() != Blocks.HOPPER) return null;
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityHopper) {
            return (TileEntityHopper) tileEntity;
        }
        return null;
    }

    public static IInventory getInventory(World world, BlockPosition position) {
        Block block = world.getType(position).getBlock();
        if (block instanceof BlockChest) {
            return ((BlockChest) block).f(world, position);
        }
        if (block.isTileEntity()) {
            TileEntity tile = world.getTileEntity(position);
            if (tile instanceof IInventory) return (IInventory) tile;
        }
        return null;
    }

}