package me.rentix07.mm.cfb.grid;/*
 * Created on 29.12.2019 09:12
 * by Pawel
 */

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public
class StageBlockPlacementValidation
{
    private final BlockPosGrid blockGrid;

    public
    StageBlockPlacementValidation(BlockPosGrid blockGrid)
    {
        this.blockGrid = blockGrid;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent evt)
    {
        Minecraft.getInstance().player.sendMessage(new TextComponentString("block evt"));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.EntityPlaceEvent evt)
    {
        Minecraft.getInstance().player.sendMessage(new TextComponentString("BE.EntityPlace evt"));
    }
}
