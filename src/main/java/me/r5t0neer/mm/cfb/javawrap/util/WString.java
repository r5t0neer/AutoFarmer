package me.rentix07.mm.cfb.javawrap.util;/*
 * Created on 29.12.2019 09:27
 * by Pawel
 */

public
class WString
{
    public static boolean beginsWith(String contextString, String beginString)
    {
        if(beginString.length() > contextString.length())
            return false;

        for(int i=-1, sz=beginString.length(); ++i < sz;)
        {
            if(contextString.charAt(i) != beginString.charAt(i))
                return false;
        }

        return true;
    }
}
