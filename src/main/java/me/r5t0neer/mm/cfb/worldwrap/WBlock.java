package me.rentix07.mm.cfb.worldwrap;/*
 * Created on 29.12.2019 09:18
 * by Pawel
 */

import me.rentix07.mm.cfb.binary.ToBytes;

import java.math.BigInteger;

public
class WBlock
{
    public final int x,z;
    public final byte y;
    public final String key;
    public final
    BigInteger posHash;

    public
    WBlock(int x, int z, byte y, String key)
    {
        this.x    = x;
        this.z    = z;
        this.y    = y;
        this.key  = key;

        {
            byte[] bytes = new byte[9];
            ToBytes.toByteArray(x, bytes, 0);
            ToBytes.toByteArray(z, bytes, 0);
            bytes[8] = y;

            this.posHash = new BigInteger(bytes);
        }
    }
}
