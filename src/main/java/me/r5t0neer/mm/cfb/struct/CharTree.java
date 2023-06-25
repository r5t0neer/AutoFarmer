package me.rentix07.mm.cfb.struct;/*
 * Created on 29.12.2019 09:45
 * by Pawel
 */

import java.util.HashMap;
import java.util.Map;

public
class CharTree
{
    private final
    Map<Character, CharTree> characterMap;

    public
    CharTree()
    {
        this.characterMap = new HashMap<>();
    }

    public void add(String str)
    {
        if(str.isEmpty())
            return;

        char contextChar = str.charAt(0);
        CharTree tree = characterMap.get(contextChar);

        if(tree == null)
        {

        }
    }
}
