package me.rentix07.mm.cfb.grid;/*
 * Created on 29.12.2019 09:12
 * by Pawel
 */

import me.rentix07.mm.cfb.math.Position;

import java.util.List;

public
class BlockPosGrid
{
    public final String         contextBlockKey;
    public final List<Position> blocksGrid;

    public
    BlockPosGrid(String contextBlockKey, List<Position> blocksGrid)
    {
        this.contextBlockKey = contextBlockKey;
        this.blocksGrid = blocksGrid;
    }
}
