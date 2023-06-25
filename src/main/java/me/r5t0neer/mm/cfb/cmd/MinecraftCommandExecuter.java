package me.rentix07.mm.cfb.cmd;/*
 * Created on 29.12.2019 13:24
 * by Pawel
 */

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public
class MinecraftCommandExecuter
{
    private
    EntityPlayerSP plr;

    public
    MinecraftCommandExecuter()
    {
        plr = Minecraft.getInstance().player;
    }

    public void execute(String cmd)
    {
        plr.sendChatMessage("/"+ cmd);
    }
}
