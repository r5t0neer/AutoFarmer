package me.rentix07.mm.cfb;/*
 * Created on 28.12.2019 21:21
 * by Pawel
 */

import me.rentix07.mm.cfb.automation.movement.PlayerMover;
import me.rentix07.mm.cfb.automation.movement.PlayerViewRotator;
import me.rentix07.mm.cfb.cmd.*;
import me.rentix07.mm.cfb.evt.PickupEventLst;
import me.rentix07.mm.cfb.grid.BlockPosGridStorage;
import me.rentix07.mm.cfb.grid.GridSelector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("cfbmod")
public
class cfbmod
{
    private
    MinecraftCommandExecuter minecraftCommandExecuter;
    private
    LiveCommandFetcher liveCommandFetcher;
    private
    PrintBlockTransKeyCmd printBlockTransKeyCmd;
    private
    GridSelectCmd gridSelectCmd;
    private
    BlockPlaceCmd blockPlaceCmd;
    private FlyStateCommand flyStateCommand;

    private
    BlockPosGridStorage gridStorage;
    private
    GridSelector gridSelector;

    private static
    PickupEventLst pickupEventLst;


    private
    PlayerViewRotator playerViewRotator;
    private
    PlayerMover playerMover;


    public
    cfbmod()
    {
        minecraftCommandExecuter = new MinecraftCommandExecuter();
        liveCommandFetcher = new LiveCommandFetcher();

        playerViewRotator = new PlayerViewRotator();
        playerMover = new PlayerMover(playerViewRotator, liveCommandFetcher);

        gridStorage = new BlockPosGridStorage();
        gridSelector = new GridSelector();

        gridSelectCmd = new GridSelectCmd(gridSelector, gridStorage);
        printBlockTransKeyCmd = new PrintBlockTransKeyCmd();
        blockPlaceCmd = new BlockPlaceCmd(playerMover);
        flyStateCommand = new FlyStateCommand(playerMover.getFlyHandler());

        liveCommandFetcher.setExecutor("gridselect", gridSelectCmd);
        liveCommandFetcher.setExecutor("printbtk", printBlockTransKeyCmd);
        liveCommandFetcher.setExecutor("bplace", blockPlaceCmd);
        liveCommandFetcher.setExecutor("flystate", flyStateCommand);

        pickupEventLst = new PickupEventLst();

        MinecraftForge.EVENT_BUS.register(pickupEventLst);
    }
}
