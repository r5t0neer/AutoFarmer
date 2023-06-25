package me.rentix07.mm.cfb.math;/*
 * Created on 29.12.2019 10:13
 * by Pawel
 */

import net.minecraft.entity.player.EntityPlayer;

public
class Position
{
    public int x,y,z;

    public
    Position(int x, int z, int y)
    {
        this.x = x;
        this.z = z;
        this.y = y;
    }

    public Position(EntityPlayer player)
    {
        this.x = (int)player.posX;
        this.y = (int)player.posY;
        this.z = (int)player.posZ;
    }

    public boolean isLower(Position another)
    {
        return y < another.y || (
                y == another.y && (
                        x < another.x ||
                                (x == another.x && z < another.z)
                )
        );
    }
}
