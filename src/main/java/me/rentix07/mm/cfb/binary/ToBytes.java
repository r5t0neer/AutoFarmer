package me.rentix07.mm.cfb.binary;/*
 * Created on 29.12.2019 09:54
 * by Pawel
 */

public
class ToBytes
{
    public static void toByteArray(int value, byte[] targetArray, int arrayBeginIndex)
    {
        assert targetArray.length >= (arrayBeginIndex + 4);

        int mask = 0xff000000;
        int shiftRCount = 3 * 8;
        for(int i=-1; ++i < 4; ++arrayBeginIndex, mask >>= 8, shiftRCount -= 8)
        {
            targetArray[arrayBeginIndex] = (byte)((value & mask) >> shiftRCount);
        }
    }
}
