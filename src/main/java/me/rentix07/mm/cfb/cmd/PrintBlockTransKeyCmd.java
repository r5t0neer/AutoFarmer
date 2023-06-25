package me.rentix07.mm.cfb.cmd;/*
 * Created on 29.12.2019 11:47
 * by Pawel
 */

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public
class PrintBlockTransKeyCmd implements ILiveCommandExecutor
{
    private static final String NO_BLOCK_BELOW_MSG = "There is no block below you.";

    @Override
    public
    void onCommand(String[] args)
    {
        Minecraft mc = Minecraft.getInstance();
        EntityPlayer plr = mc.player;

        if(plr.posY == 0)
        {
            plr.sendMessage(new TextComponentString(NO_BLOCK_BELOW_MSG));
            return;
        }

        IBlockState blockState = mc.world.getBlockState(new BlockPos((int)plr.posX, (int)plr.posY-1, (int)plr.posZ));
        if(blockState != null && !blockState.getMaterial().equals(Material.AIR))
        {
            plr.sendMessage(new TextComponentString("Block below has translation key "+ blockState.getBlock().getTranslationKey()));
            if(blockState.getMaterial().blocksMovement())
                plr.sendMessage(new TextComponentString("It blocks movement."));
            else plr.sendMessage(new TextComponentString("It does not block movement."));
        }
        else
        {
            plr.sendMessage(new TextComponentString(NO_BLOCK_BELOW_MSG));
        }
    }
}
