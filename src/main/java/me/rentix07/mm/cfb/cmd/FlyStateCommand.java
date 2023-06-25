package me.rentix07.mm.cfb.cmd;/*
 * Created on 30.12.2019 21:16
 * by Pawel
 */

import me.rentix07.mm.cfb.automation.movement.PlayerFlyStateSimpleFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public
class FlyStateCommand implements ILiveCommandExecutor
{
    private PlayerFlyStateSimpleFetcher flyHandler;

    public
    FlyStateCommand(PlayerFlyStateSimpleFetcher flyHandler)
    {
        this.flyHandler = flyHandler;
    }

    @Override
    public
    void onCommand(String[] args)
    {
        Minecraft.getInstance().player.sendMessage(new TextComponentString("Player is "+ (flyHandler.isProbablyFlying() ? "" : "not ") +"flying."));
    }
}
