package me.rentix07.mm.cfb.struct;/*
 * Created on 29.12.2019 09:20
 * by Pawel
 */

import me.rentix07.mm.cfb.javawrap.util.WString;

import java.util.HashMap;
import java.util.Map;

public
class StringMap<T>
{
    public final T value;

    private final
    Map<String, StringMap> subStringMap;

    public
    StringMap(T value)
    {
        this.value = value;
        this.subStringMap = new HashMap<>();
    }

    public void add(String key, T value)
    {
        if(subStringMap.isEmpty())
        {
            subStringMap.put(key, new StringMap<T>(value));
        }
    }

    public boolean contains(String str)
    {
        if(subStringMap.isEmpty())
            return false;
        else
        {
            int strLen = str.length();

            for(Map.Entry<String, StringMap> entry : subStringMap.entrySet())
            {
                String sub = entry.getKey();

                if(sub.length() == strLen && sub.equals(str))
                {
                    return true;
                }
                if(WString.beginsWith(str, sub))
                {
                    return entry.getValue().contains(str.substring(0, sub.length()));
                }
            }
        }

        return false;
    }
}
