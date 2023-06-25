package me.rentix07.mm.cfb.automation.movement;/*
 * Created on 29.12.2019 13:39
 * by Pawel
 */

import me.rentix07.mm.cfb.cmd.ILiveCommandExecutor;
import me.rentix07.mm.cfb.cmd.LiveCommandFetcher;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.glfw.GLFW;

public
class PlayerFlyStateSimpleFetcher
{
    private
    Minecraft mc;
    private long    spacePressedLastTime;
    private boolean lastSpaceWasCandidate;
    private boolean flyingState;
    private int     flyStateChangeChained;

    private boolean scheduledForcedFly;
    private int schFF_spacePressCount;

    private int flyfetchtime = 350;

    class FlyFetchTimeCommand implements ILiveCommandExecutor
    {
        @Override
        public
        void onCommand(String[] args)
        {
            if(args.length == 1)
            {
                try
                {
                    int milli = Integer.parseInt(args[0]);
                    if(milli < 1)
                        throw new NumberFormatException();

                    flyfetchtime = milli;
                }
                catch (NumberFormatException e)
                {
                    mc.player.sendMessage(new TextComponentString("Nr of milliseconds must be natural positive number."));
                }
            }
            else mc.player.sendMessage(new TextComponentString("Type nr of milliseconds as first argument."));
        }
    }

    private FlyFetchTimeCommand flyFetchTimeCommand;

    public
    PlayerFlyStateSimpleFetcher(LiveCommandFetcher liveCommandFetcher)
    {
        mc                   = Minecraft.getInstance();
        //flyFetchTimeCommand = new FlyFetchTimeCommand();
        //liveCommandFetcher.setExecutor("flyfetchtime", flyFetchTimeCommand);

        spacePressedLastTime  = 0;
        lastSpaceWasCandidate = false;
        flyingState           = false;
        flyStateChangeChained = 0;

        scheduledForcedFly = false;
        schFF_spacePressCount = 0;

        MinecraftForge.EVENT_BUS.register(this);

        GLFW.glfwSetKeyCallback(mc.mainWindow.getHandle(), this::onGLFWKeyEvent);
    }

    public boolean isProbablyFlying()
    {
        return flyingState;
    }

    public void forceFly()
    {
        if(flyingState)
            return;

        schFF_spacePressCount = 0;
        scheduledForcedFly = true;
    }

    private boolean isPlayerOnBlock()
    {
        Minecraft      mc    = Minecraft.getInstance();
        EntityPlayerSP plr   = mc.player;
        WorldClient    world = mc.world;

        // this case can be buggy as it doesn't care non-solid block height (or rather player posY in relation to block height)
        IBlockState blockState = world.getBlockState(new BlockPos((int)plr.posX, (int)plr.posY, (int)plr.posZ));
        boolean isProperBlock = blockState != null && !blockState.getMaterial().equals(Material.AIR) && blockState.getMaterial().blocksMovement();

        // this case not
        if(!isProperBlock)
        {
            blockState = world.getBlockState(new BlockPos((int)plr.posX, (int)plr.posY-1, (int)plr.posZ));
            isProperBlock = (plr.posY - Math.floor(plr.posY)) < 0.1 && blockState != null && !blockState.getMaterial().equals(Material.AIR) && blockState.getMaterial().blocksMovement();
        }

        return isProperBlock;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onFallEvent(PlayerFlyableFallEvent event)
    {
        /*if(flyingState)
        {
            flyingState = false;
            System.out.println("update fly state to off [FallEvent]");
        }*/
    }

    public void onGLFWKeyEvent(long windowPointer, int key, int scanCode, int action, int modifiers)
    {
        Minecraft.getInstance().keyboardListener.onKeyEvent(windowPointer, key, scanCode, action, modifiers);

        if(action == GLFW.GLFW_PRESS)
        {
            onKeyPressedEvent(key, scanCode);
        }
        else if(action == GLFW.GLFW_RELEASE)
        {
            onKeyReleased(key);
        }
    }

    public void onKeyPressedEvent(int key, int scanCode)
    {
        if(key == GLFW.GLFW_KEY_LEFT_SHIFT)
        {
            //System.out.println("left shift pressed/released");

            if(isPlayerOnBlock())
            {
                if(flyingState)
                {
                    flyingState = false;
                    System.out.println("flying state updated to off [left shift release]");
                    return;
                }
            }
        }

        if(key == GLFW.GLFW_KEY_SPACE)
        {
            //if(key == GLFW.GLFW_KEY_LEFT_SHIFT)
            {
                //System.out.println("left shift pressed/released");

                if(isPlayerOnBlock())
                {
                    if(flyingState)
                    {
                        flyingState = false;
                        System.out.println("flying state updated to off [space pressed]");
                        return;
                    }
                }
            }

            //System.out.println("space pressed");

            long time = System.currentTimeMillis();
            long timeDiff = time - spacePressedLastTime;

            if(lastSpaceWasCandidate)
            {
                lastSpaceWasCandidate = false;
            }
            else if (timeDiff < 300/*flyfetchtime*/)
            {
                flyingState = !flyingState;
                lastSpaceWasCandidate = true;

                System.out.println("flying state updated to " + (flyingState ? "on" : "off") + " [space]");
            }

            spacePressedLastTime = time;
        }
    }

    public void onKeyReleased(int key)
    {
        if(key == GLFW.GLFW_KEY_LEFT_SHIFT)
        {
            //System.out.println("left shift pressed/released");

            if(isPlayerOnBlock())
            {
                if(flyingState)
                {
                    flyingState = false;
                    System.out.println("flying state updated to off [left shift release]");
                }
            }
        }
    }

    long lastTickTime = System.currentTimeMillis() ;
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onLeftEvent(TickEvent.ClientTickEvent event)
    {
        if(mc.playerController == null || mc.player == null)
        {
            if(flyingState)
            {
                flyingState = false;// todo doesn't work on exit from singleplayer world[tested] or server[not tested]
                System.out.println("flying state updated to off, probably left server or local world");
            }

            return;
        }
        else if(scheduledForcedFly)
        {
            long time = System.currentTimeMillis();
            System.out.println("WorldTick "+ (time - lastTickTime) +"ms");
            lastTickTime = time;

            /*if(schFF_spacePressCount > 0 && (schFF_spacePressCount+2)%3 == 0 )
            {
                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_RELEASE,
                        0
                );
                System.out.println("[ff] released space query");
            }

            if((schFF_spacePressCount+3)%3 == 0 && schFF_spacePressCount < 4)
            {
                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_PRESS,
                        0
                );
                System.out.println("[ff] pressed space query");
            }*/

            if(++schFF_spacePressCount == 6)
            {
                if(!flyingState)
                {
                    schFF_spacePressCount = 0;
                    System.out.println("Forcing Fly Error");//, Retrying...");
                    //return;
                }
                else System.out.println("Forcing Fly Done");

                scheduledForcedFly = false;
                schFF_spacePressCount = 0;

                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_RELEASE,
                        0
                );
            }
            else
            {
                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_PRESS,
                        0
                );
            }

            /*if(++schFF_spacePressCount == 2)
            {
                if(!flyingState)
                {
                    System.out.println("Forcing Fly Error by altcode");
                }
                else System.out.println("Forcing Fly Done By altcode");

                scheduledForcedFly = false;
                schFF_spacePressCount = 0;
            }
            else
            {
                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_PRESS,
                        0
                );

                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_RELEASE,
                        0
                );

                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_PRESS,
                        0
                );

                mc.keyboardListener.onKeyEvent(
                        mc.mainWindow.getHandle(),
                        GLFW.GLFW_KEY_SPACE,
                        GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SPACE),
                        GLFW.GLFW_RELEASE,
                        0
                );
            }*/
        }
    }
}
