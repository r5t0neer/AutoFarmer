package me.rentix07.mm.cfb.grid;/*
 * Created on 29.12.2019 12:49
 * by Pawel
 */

import me.rentix07.mm.cfb.math.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public
class BlockPosGridStorage
{
    public final
    Map<String, BlockPosGrid> gridMap;

    public
    BlockPosGridStorage()
    {
        this.gridMap = new HashMap<>();
    }

    public boolean removeGrid(String label)
    {
        BlockPosGrid grid = gridMap.get(label);

        if(grid == null)
            return false;

        gridMap.remove(label);

        return true;
    }

    public boolean addNewFromSelector(GridSelector selector)
    {
        BlockPosGrid grid = gridMap.get(selector.label);

        if(grid != null)
            return false;

        gridMap.put(selector.label, new BlockPosGrid(selector.gridBlocksKey, new ArrayList<>()));
        List<Position> gridPoses = gridMap.get(selector.label).blocksGrid;
        gridPoses.addAll(selector.gridBlocks);

        return true;
    }
}
