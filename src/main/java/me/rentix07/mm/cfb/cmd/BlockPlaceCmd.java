package me.rentix07.mm.cfb.cmd;/*
 * Created on 29.12.2019 13:23
 * by Pawel
 */

import me.rentix07.mm.cfb.automation.movement.PlayerMover;
import me.rentix07.mm.cfb.automation.movement.PlayerViewRotator;
import me.rentix07.mm.cfb.math.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public
class BlockPlaceCmd implements ILiveCommandExecutor
{
    PlayerViewRotator viewRotator;
    PlayerMover playerMover;

    public
    BlockPlaceCmd(PlayerMover mover)
    {
        playerMover = mover;
        viewRotator = mover.getViewRotator();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private int ffDelay=-1;
    private int delay =0;
    private int time  =0;
    private int mx=0,my=0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTickEvt(TickEvent.RenderTickEvent event)
    {
        if(Minecraft.getInstance().player == null)
            return;

        if(ffDelay > 0)
        {
            --ffDelay;
        }
        else if(ffDelay == 0)
        {
            --ffDelay;

            System.out.println("forcing...");
            playerMover.forceFly();
        }

        if(time > 0)
        {
            if(delay != 0)
            {
                --delay;
            }
            else
            {
                /*try
                {
                    Minecraft mc = Minecraft.getInstance();
                    MouseHelper mouseHelper = mc.mouseHelper;
                    Method method = mouseHelper.getClass().getDeclaredMethod("cursorPosCallback", long.class, double.class, double.class);
                    method.setAccessible(true);
                    method.invoke(mouseHelper, mc.mainWindow.getHandle(), mx-=10.0, my+=1.0);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }*/

                --time;
            }
        }
    }

    @Override
    public
    void onCommand(String[] args)
    {
        //playerMover.alignCameraToStandardDirection();

        /*Minecraft mc = Minecraft.getInstance();
        MouseHelper mouseHelper = mc.mouseHelper;
        mx = (int)mouseHelper.getMouseX();
        my = (int)mouseHelper.getMouseY();


        time = 20;
        delay = 30;*/
        if(args.length == 0)
            viewRotator.alignCameraToStandardDirection();
        else if(args[0].equalsIgnoreCase("nvm"))// naive move
        {
            EntityPlayerSP plr = Minecraft.getInstance().player;

            if(args.length != 4)
            {
                plr.sendMessage(new TextComponentString("Define target x, y and z as natural numbers."));
                return;
            }

            int x,y,z;
            try
            {
                x = Integer.parseInt(args[1]);
                y = Integer.parseInt(args[2]);
                z = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException e)
            {
                plr.sendMessage(new TextComponentString("Define target x, y and z as natural numbers."));
                return;
            }

            playerMover.moveNaivelyTo(new Position(x,z,y));
        }
        else if(args[0].equalsIgnoreCase("ff"))
        {
            ffDelay = 40;
        }
        else if(args[0].equalsIgnoreCase("printospeed"))
        {
            EntityPlayerSP plr = Minecraft.getInstance().player;
            plr.sendMessage(new TextComponentString(""+playerMover.getOriginalSpeed()));
        }
        else if(args[0].equalsIgnoreCase("printcspeed"))
        {
            EntityPlayerSP plr = Minecraft.getInstance().player;
            plr.sendMessage(new TextComponentString(""+playerMover.getRetrievedCurrentSpeed()));
        }
        else if(args[0].equalsIgnoreCase("lowspeed"))
        {
            if(args.length == 1)
                playerMover.setLowMovementSpeed();
            else
            {
                try {
                    double speed = Double.parseDouble(args[1]);
                    playerMover.setLowSpeed(speed);
                    playerMover.setLowMovementSpeed();
                }
                catch (NumberFormatException e)
                {
                    EntityPlayerSP plr = Minecraft.getInstance().player;
                    plr.sendMessage(new TextComponentString("Type a floating point number or natural one."));
                }
            }
        }
        else if(args[0].equalsIgnoreCase("listcap"))
        {
            try
            {
                Minecraft      mc    = Minecraft.getInstance();
                EntityPlayerSP plr   = mc.player;
                Class clazz = plr.getClass();
                /*int depth = -1;
                do
                {
                    ++depth;

                    for(Field field : clazz.getDeclaredFields())
                    {
                        if(field.getName().equalsIgnoreCase("capabilities"))
                        {
                            plr.sendMessage(new TextComponentString("Found at superclass nr "+ depth));// 6
                            return;
                        }
                    }
                }
                while ((clazz = clazz.getSuperclass()) != null);

                plr.sendMessage(new TextComponentString("ended finding"));*/

                clazz = clazz.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass();
                Field          field = clazz.getDeclaredField("capabilities");
                field.setAccessible(true);
                plr.sendMessage(new TextComponentString("[REFLECT] obj class: "+ field.getType().toString()));
                boolean isnull = field.get(plr) == null;
                plr.sendMessage(new TextComponentString("[REFLECT] is obj null: "+ (isnull ? "true" : "false")));
                if(!isnull)
                    plr.sendMessage(new TextComponentString("[REFLECT] is obj instanceof: "+
                                                                    (field.get(clazz) instanceof CapabilityDispatcher ? "true" : "false"))
                    );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Minecraft.getInstance().player.sendMessage(new TextComponentString("rotYaw="+ Minecraft.getInstance().player.rotationYaw));
        }
    }
}
