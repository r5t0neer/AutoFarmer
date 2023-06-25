package me.rentix07.mm.cfb.cmd;/*
 * Created on 29.12.2019 12:40
 * by Pawel
 */

import me.rentix07.mm.cfb.math.Position;
import me.rentix07.mm.cfb.grid.GridSelector;
import me.rentix07.mm.cfb.grid.BlockPosGridStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

public
class GridSelectCmd implements ILiveCommandExecutor
{
    private static final String GRID_IS_EMPTY_MSG = "The grid has no block poses.";

    private
    GridSelector gridSelector;
    private BlockPosGridStorage gridStorage;

    private
    Position pos1, pos2;

    public
    GridSelectCmd(GridSelector gridSelector, BlockPosGridStorage gridStorage)
    {
        this.gridSelector = gridSelector;
        this.gridStorage  = gridStorage;
        this.pos1         = null;
        this.pos2         = null;
    }

    @Override
    public
    void onCommand(String[] args)
    {
        EntityPlayer player = Minecraft.getInstance().player;

        if(args.length == 0)
        {
            player.sendMessage(new TextComponentString("1st arg must be specified"));
        }
        else
        {
            String arg0 = args[0];
            if(arg0.equalsIgnoreCase("pos1"))
            {
                pos1 = new Position(player);
                player.sendMessage(new TextComponentString("Pos1 - x: "+ pos1.x +", y: "+ pos1.y +", z: "+ ((int)(pos1.z) & 0xff)));
            }
            else if(arg0.equalsIgnoreCase("pos2"))
            {
                pos2 = new Position(player);
                player.sendMessage(new TextComponentString("Pos2 - x: "+ pos2.x +", y: "+ pos2.y +", z: "+ ((int)(pos2.z) & 0xff)));
            }
            else if(arg0.equalsIgnoreCase("extract"))
            {
                if(args.length == 3)
                {
                    String blockKey = args[1];
                    if(!gridSelector.extractAndLocalizeBlockPosGrid(pos1, pos2, blockKey))
                    {
                        player.sendMessage(new TextComponentString("No pos1 or pos2 or both."));
                    }

                    if(gridSelector.gridBlocks.isEmpty())
                    {
                        player.sendMessage(new TextComponentString(GRID_IS_EMPTY_MSG));
                        return;
                    }

                    if(!gridStorage.addNewFromSelector(gridSelector))
                    {
                        player.sendMessage(new TextComponentString("There is already a grid for same label."));
                        player.sendMessage(new TextComponentString("Use /gridselector label <new label>"));
                    }
                    else player.sendMessage(new TextComponentString("Added grid for label "+ gridSelector.label));
                }
                else
                {
                    player.sendMessage(new TextComponentString("Need to specify block key/type AND grid label."));
                }
            }
            else if(arg0.equalsIgnoreCase("label"))
            {
                if(args.length == 2)
                {
                    if(gridSelector.gridBlocks.isEmpty())
                    {
                        player.sendMessage(new TextComponentString(GRID_IS_EMPTY_MSG));
                        return;
                    }

                    String newLabel = args[1];

                    if(!newLabel.equalsIgnoreCase(gridSelector.label))
                    {
                        player.sendMessage(new TextComponentString("Label cannot be same."));
                    }
                    else
                    {
                        gridSelector.label = args[1];
                        player.sendMessage(new TextComponentString("Changed label to "+ newLabel));

                        gridStorage.addNewFromSelector(gridSelector);
                        player.sendMessage(new TextComponentString("Added grid for label "+ gridSelector.label));
                    }
                }
                else
                {
                    player.sendMessage(new TextComponentString("Need to specify grid new label."));
                }
            }
            else if(arg0.equalsIgnoreCase("rm"))
            {
                if(args.length == 2)
                {
                    String label = args[1];
                    if(!gridStorage.removeGrid(label))
                    {
                        player.sendMessage(new TextComponentString("Grid for label "+ label +" does not exist."));
                    }
                    else player.sendMessage(new TextComponentString("Removed grid for label "+ label));
                }
                else player.sendMessage(new TextComponentString("Specify a label for grid that has to be removed."));
            }
        }
    }
}
