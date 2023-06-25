package me.rentix07.mm.cfb.automation.movement;/*
 * Created on 29.12.2019 22:24
 * by Pawel
 */

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.lang.reflect.Method;

public
class PlayerViewRotator
{
    private enum STEP { MEASURE, EXECUTE }

    private class MeasureFrame
    {
        double m_mouseX, m_yaw;

        public
        MeasureFrame(double m_mouseX, double m_yaw)
        {
            this.m_mouseX = m_mouseX;
            this.m_yaw    = m_yaw;
        }
    }

    private int keyToLeft, keyToRight, keyForward, keyBackward;

    private boolean      alignViewQuery;
    private int delay;
    private STEP alignViewStage;

    private int currentFrame;
    private MeasureFrame aVQMFrame1/*, aVQMFrame2*/;
    private double mouseXMoveFor1Degree;

    private final double VIEW_ALIGN_MOTION_DEGREES_STEP = 4.41;//6.0;
    private double       aVQMouseX, aVQMouseY;

    private Minecraft mc;
    private EntityPlayerSP plr;

    public
    PlayerViewRotator()
    {
        this.alignViewQuery = false;

        this.mc = Minecraft.getInstance();
        this.plr = mc.player;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void determineKeys()
    {
        EntityPlayerSP plr = mc.player;
        if(plr == null)
        {
            throw new UnsupportedOperationException();
        }


    }

    public void alignCameraToStandardDirection()
    {
        Minecraft.getInstance().player.sendMessage(new TextComponentString("alignViewQuery = "+ (alignViewQuery ? "true => not started" : "false => starting...")));

        if(alignViewQuery)
            return;

        MouseHelper mouseHelper = mc.mouseHelper;
        aVQMouseX        = mouseHelper.getMouseX();
        aVQMouseY        = mouseHelper.getMouseY();

        delay = 40;
        currentFrame = 0;// none
        alignViewStage = STEP.MEASURE;
        alignViewQuery = true;
    }

    public boolean isAligningView()
    {
        return alignViewQuery;
    }

    // todo change speed curve to eg ease-in-out time function

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTickEvent(TickEvent.ClientTickEvent event)
    {
        if (alignViewQuery)
        {
            if(delay > 0)
            {
                --delay;
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            EntityPlayerSP plr = mc.player;

            if(plr == null)
            {
                System.out.println("Player = null");
                return;
                //throw new UnsupportedOperationException();
            }

            double mvx=0;
            double dYaw = (double)plr.rotationYaw;
            while(dYaw > 360.0d)
            {
                long lYaw = ((long)dYaw);
                double rest = dYaw - ((double)lYaw);

                dYaw = (double)(lYaw % 360L);
                dYaw += rest;
            }
            while (dYaw < 0.0d)
            {
                dYaw += 360d;
            }

            switch(alignViewStage)
            {
                case EXECUTE:
                    if (dYaw != 180.0d)
                    {
                        //System.out.println("dYaw = "+ dYaw);
                        double degreeDist = 180.0 - dYaw;

                        if(degreeDist > 0)
                        {
                            mvx = Math.min(degreeDist, VIEW_ALIGN_MOTION_DEGREES_STEP);
                        }
                        else
                        {
                            //System.out.println("2");
                            degreeDist = dYaw - 180.0d;

                            mvx = -Math.min(degreeDist, VIEW_ALIGN_MOTION_DEGREES_STEP);
                        }
                        mvx *= mouseXMoveFor1Degree;

                        //System.out.println("next & current -> "+ mouseXMoveFor1Degree +" => "+ mvx +" + "+ aVQMouseX);

                        mouseXMoveFor1Degree = Math.abs(aVQMouseX - aVQMFrame1.m_mouseX)
                                /
                                Math.abs(dYaw - aVQMFrame1.m_yaw);

                        aVQMFrame1 = new MeasureFrame(aVQMouseX, dYaw);
                    }
                    else
                    {
                        alignViewQuery = false;
                        System.out.println("reached 180.0f");
                        return;
                    }
                    break;

                case MEASURE:
                    mvx = (180.0d - dYaw) > 0 ? VIEW_ALIGN_MOTION_DEGREES_STEP : (-VIEW_ALIGN_MOTION_DEGREES_STEP);

                    switch (currentFrame)
                    {
                        case 0: aVQMFrame1 = new MeasureFrame(aVQMouseX, dYaw); currentFrame=1; break;
                        case 1: /*aVQMFrame2 = measureFrame;*/ currentFrame=0;
                            mouseXMoveFor1Degree = Math.abs(aVQMouseX - aVQMFrame1.m_mouseX)
                                            /
                                            Math.abs(dYaw - aVQMFrame1.m_yaw);

                            /*plr.sendMessage(new TextComponentString("mouseXMoveFor1Degree: "+mouseXMoveFor1Degree));
                            plr.sendMessage(new TextComponentString("frame1: x"+aVQMFrame1.m_mouseX+" yaw"+aVQMFrame1.m_yaw));
                            plr.sendMessage(new TextComponentString("current/frame2: x"+aVQMouseX+"(+"+ mvx +") yaw"+dYaw));*/

                            aVQMFrame1 = new MeasureFrame(aVQMouseX, dYaw);

                            alignViewStage = STEP.EXECUTE;
                            //plr.sendMessage(new TextComponentString("Switched to EXECUTE stage"));
                            break;
                    }
                    break;
            }

            if(mvx != 0)
            {
                moveMouseTo(aVQMouseX += mvx, aVQMouseY);
            }
        }
    }

    private void moveMouseTo(double x, double y)
    {
        try
        {
            Minecraft mc = Minecraft.getInstance();
            MouseHelper mouseHelper = mc.mouseHelper;
            Method      method      = mouseHelper.getClass().getDeclaredMethod("cursorPosCallback", long.class, double.class, double.class);
            method.setAccessible(true);
            method.invoke(mouseHelper, mc.mainWindow.getHandle(), x, y);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
