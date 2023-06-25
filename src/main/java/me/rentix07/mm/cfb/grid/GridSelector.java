package me.rentix07.mm.cfb.grid;/*
 * Created on 29.12.2019 10:10
 * by Pawel
 */

import me.rentix07.mm.cfb.math.Position;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public
class GridSelector
{
    public
    List<Position> gridBlocks;
    public String gridBlocksKey;
    public String label;

    public
    GridSelector()
    {
        gridBlocks = new ArrayList<>();
        gridBlocksKey = "";
    }

    public boolean extractAndLocalizeBlockPosGrid(Position pos1, Position pos2, String blockKey)
    {
        if(pos1 == null || pos2 == null)
            return false;

        gridBlocks.clear();

        Minecraft mc = Minecraft.getInstance();
        EntityPlayer player = mc.player;
        player.sendMessage(new TextComponentString("Extracting..."));

        WorldClient world = mc.world;

        int lowX, lowY, lowZ, highX, highY, highZ;

        if(pos1.x > pos2.x) { highX = pos1.x; lowX = pos2.x; } else { highX = pos2.x; lowX = pos1.x; }
        if(pos1.y > pos2.y) { highY = pos1.y; lowY = pos2.y; } else { highY = pos2.y; lowY = pos1.y; }
        if(pos1.z > pos2.z) { highZ = pos1.z; lowZ = pos2.z; } else { highZ = pos2.z; lowZ = pos1.z; }

        //int         count          =0, compareCount =0;
        for(int y=lowY; y <= highY; ++y)
        {
            //System.out.println("y:"+y);

            for(int x=lowX; x <= highX; ++x)
            {
                for(int z=lowZ; z <= highZ; ++z)
                {
                    IBlockState blockState = world.getBlockState(new BlockPos(x, y, z));

                    if(blockState.getBlock().getTranslationKey().equalsIgnoreCase(blockKey))
                    {
                        //++count;
                        gridBlocks.add(new Position(x-lowX,y-lowY,z-lowZ));// normalize/localize
                    }
                }
            }
        }
        //System.out.println("added "+ count +" blocks to grid and compared "+ compareCount +" for key "+ blockKey);

        player.sendMessage(new TextComponentString("Extracting done!"));

        gridBlocksKey = blockKey;

        return true;
    }
}
