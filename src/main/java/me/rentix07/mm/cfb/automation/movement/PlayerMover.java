package me.rentix07.mm.cfb.automation.movement;/*
 * Created on 29.12.2019 13:23
 * by Pawel
 */

import me.rentix07.mm.cfb.cmd.LiveCommandFetcher;
import me.rentix07.mm.cfb.math.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.glfw.GLFW;

/*

if yaw = 180:
A -> -x
W -> -z

 */

public
class PlayerMover
{
    private Minecraft mc;
    private
    EntityPlayerSP plr;

    private PlayerFlyStateSimpleFetcher flyHandler;
    private PlayerViewRotator           viewRotator;

    private enum STATE { NONE, X, Y, Z, ENDED }
    private enum PA_STATE {MEASURE_SPEED, ALIGN_CLOSELY}

    private double originalSpeed, currentSpeed, anyMoveSpeed = 0.8;// todo doesn;t care if fly or not - fix it

    private boolean positionAlignQuery, posAlignQuery_queriedViewAlign;
    private PA_STATE positionAlignState;
    private double posAlignTargetX, posAlignTargetY, posAlignTargetZ;
    private double lastPosX, lastPosY, lastPosZ;
    private int    posAlignStateSubstep;
    private double xTickSpeed, yTickSpeed, zTickSpeed;

    private boolean  movingQuery;
    private STATE    movingState;
    private Position movingFrom, movingTarget, movingDir;
    private int currentPressedKey;




    public
    PlayerMover(PlayerViewRotator viewRotator, LiveCommandFetcher liveCommandFetcher)
    {
        this.mc = Minecraft.getInstance();
        this.plr = mc.player;

        this.flyHandler = new PlayerFlyStateSimpleFetcher(liveCommandFetcher);
        this.viewRotator = viewRotator;

        this.movingQuery    = false;

        retrieveOriginalSpeed();
        currentSpeed = originalSpeed;

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void retrieveOriginalSpeed()
    {
        try
        {
            Minecraft            mc       = Minecraft.getInstance();
            AbstractAttributeMap att      = mc.player.getAttributeMap();
            IAttributeInstance   attrInst =  att.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
            originalSpeed = attrInst.getValue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void retrieveCurrentSpeed()
    {
        double originalSpeedBuff = originalSpeed;
        retrieveOriginalSpeed();
        currentSpeed = originalSpeed;
        originalSpeed = originalSpeedBuff;
    }

    private long idgenforattr = 1000000L + (long)Math.floor(Math.random()*1000000L);

    public void setLowSpeed(double speed)
    {
        anyMoveSpeed = speed;
    }

    public
    double getOriginalSpeed()
    {
        return originalSpeed;
    }

    public double getRetrievedCurrentSpeed()
    {
        double originalSpeedBuff = originalSpeed;
        retrieveOriginalSpeed();
        double speed = originalSpeed;
        originalSpeed = originalSpeedBuff;

        return speed;
    }

    public void setLowMovementSpeed()
    {
        retrieveCurrentSpeed();
        if(currentSpeed == anyMoveSpeed)
            return;

        idgenforattr += (long)Math.floor(Math.random()*100L);

        Minecraft            mc       = Minecraft.getInstance();
        AbstractAttributeMap att      = mc.player.getAttributeMap();
        IAttributeInstance   attrInst =  att.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        attrInst.applyModifier(new AttributeModifier(String.valueOf(idgenforattr), anyMoveSpeed - originalSpeed, 0));

        currentSpeed = getRetrievedCurrentSpeed();
    }

    public void setOriginalMovementSpeed()
    {
        retrieveCurrentSpeed();
        if(currentSpeed == originalSpeed)
            return;

        idgenforattr += (long)Math.floor(Math.random()*100L);

        Minecraft            mc       = Minecraft.getInstance();
        AbstractAttributeMap att      = mc.player.getAttributeMap();
        IAttributeInstance   attrInst =  att.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        attrInst.applyModifier(new AttributeModifier(String.valueOf(idgenforattr), originalSpeed - anyMoveSpeed, 0));

        currentSpeed = getRetrievedCurrentSpeed();
    }

    public
    PlayerFlyStateSimpleFetcher getFlyHandler()
    {
        return flyHandler;
    }

    public
    PlayerViewRotator getViewRotator()
    {
        return viewRotator;
    }

    public void forceFly()
    {
        flyHandler.forceFly();
    }

    private void alignPositionCloselyToCenterOfBlock()
    {
        if(positionAlignQuery)
        {
            System.out.println("Already aligning position and moving after.");
            mc.player.sendMessage(new TextComponentString("Already aligning position and moving after."));
        }
            //throw new UnsupportedOperationException("Already aligning position and moving after.");

        EntityPlayerSP plr = mc.player;

        positionAlignState   = PA_STATE.MEASURE_SPEED;
        posAlignStateSubstep = 0;
        posAlignTargetX = Math.floor(plr.posX) + 0.5;
        posAlignTargetY = Math.floor(plr.posY) + 0.0;
        posAlignTargetZ = Math.floor(plr.posZ) + 0.5;

        posAlignQuery_queriedViewAlign = false;
        positionAlignQuery = true;
    }

    public void moveNaivelyTo(Position target) throws UnsupportedOperationException
    {


        viewRotator.alignCameraToStandardDirection();
        alignPositionCloselyToCenterOfBlock();
        //forceFly();

        if(movingQuery)
            throw new UnsupportedOperationException("Already moving.");


        Minecraft mc = Minecraft.getInstance();
        EntityPlayerSP plr = mc.player;

        movingState = STATE.NONE;
        movingFrom = new Position((int)plr.posX, (int)plr.posZ, (int)plr.posY);
        movingTarget = target;
        movingDir = new Position(
                Integer.compare(movingTarget.x, movingFrom.x),// from->target is 0...1...
                Integer.compare(movingTarget.z, movingFrom.z),
                Integer.compare(movingTarget.y, movingFrom.y)
        );
        currentPressedKey = 0;

        movingQuery = true;
    }

    long lastCTETime = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTickEvent(TickEvent.ClientTickEvent event)
    {
        long time = System.currentTimeMillis();
        if(lastCTETime == 0 || (time - lastCTETime) < 30)
        {
            lastCTETime = time;
            return;
        }

        if(mc.player == null || mc.playerController == null)
        {
            positionAlignQuery = false;
            movingQuery = false;
            return;
        }

        if(viewRotator.isAligningView()){}
        else if(positionAlignQuery)
        {
            if(!posAlignQuery_queriedViewAlign && !viewRotator.isAligningView())
            {
                posAlignQuery_queriedViewAlign = true;
                try{viewRotator.alignCameraToStandardDirection();}
                catch (Exception e) { e.printStackTrace(); }
                return;
            }

            EntityPlayerSP plr = Minecraft.getInstance().player;

            switch (positionAlignState)
            {
                case MEASURE_SPEED:
                    System.out.println("pos align MEASURE_SPEED "+ posAlignStateSubstep);
                    switch(posAlignStateSubstep)
                    {
                        // making changes in player position...

                        case 0:// begin x
                            posAlignStateSubstep =1;

                            lastPosX = plr.posX;

                            currentPressedKey = GLFW.GLFW_KEY_D;
                            pressCurrentKey();
                            break;
                        case 1:// xspeed; begin z
                            releaseCurrentKey();
                            posAlignStateSubstep = 2;
                            xTickSpeed = Math.abs(plr.posX - lastPosX);

                            lastPosZ = plr.posZ;

                            currentPressedKey = GLFW.GLFW_KEY_S;
                            pressCurrentKey();
                            break;
                        case 2:// zspeed; begin y
                            releaseCurrentKey();
                            posAlignStateSubstep = 3;
                            zTickSpeed = Math.abs(plr.posZ - lastPosZ);

                            lastPosY = plr.posY;

                            currentPressedKey = GLFW.GLFW_KEY_SPACE;
                            pressCurrentKey();
                            break;
                        case 3:// yspeed
                            releaseCurrentKey();
                            posAlignStateSubstep = 4;
                            yTickSpeed = Math.abs(plr.posY - lastPosY);
                            break;

                        // and backing it up/revert it back then...

                        case 4://back x: begin
                            posAlignStateSubstep = 5;
                            currentPressedKey = GLFW.GLFW_KEY_A;
                            pressCurrentKey();
                            break;
                        case 5:
                            releaseCurrentKey();
                            posAlignStateSubstep = 6;
                            currentPressedKey = GLFW.GLFW_KEY_W;
                            pressCurrentKey();
                            break;
                        case 6:
                            releaseCurrentKey();
                            posAlignStateSubstep = 7;
                            currentPressedKey = GLFW.GLFW_KEY_LEFT_SHIFT;
                            pressCurrentKey();
                            break;
                        case 7:
                            releaseCurrentKey();
                            posAlignStateSubstep = 0;
                            positionAlignState = PA_STATE.ALIGN_CLOSELY;
                            break;
                    }
                    break;

                case ALIGN_CLOSELY:
                    System.out.println("pos align ALIGN_CLOSELY "+ posAlignStateSubstep);
                    switch (posAlignStateSubstep)
                    {
                        case 0:// xbegin
                            if(plr.posX > posAlignTargetX)
                            {
                                if (Math.floor(plr.posX - xTickSpeed) < posAlignTargetX)
                                {
                                    posAlignStateSubstep = 2;
                                    return;
                                }

                                currentPressedKey    = GLFW.GLFW_KEY_A;
                                System.out.println("MOVING LEFT");
                            }
                            else
                            {
                                if (Math.floor(plr.posX + xTickSpeed) > posAlignTargetX)
                                {
                                    posAlignStateSubstep = 2;
                                    return;
                                }

                                currentPressedKey    = GLFW.GLFW_KEY_D;
                                System.out.println("MOVING RIGHT");
                            }

                            posAlignStateSubstep = 1;
                            pressCurrentKey();
                            break;
                        case 1:// until x
                            if (xTickSpeed <= 0.01 || currentPressedKey == GLFW.GLFW_KEY_A
                                ? Math.floor(plr.posX - xTickSpeed) < posAlignTargetX
                                : Math.floor(plr.posX + xTickSpeed) > posAlignTargetX)
                            {
                                releaseCurrentKey();
                                posAlignStateSubstep = 2;
                                return;
                            }
                            break;
                        case 2:// zbegin
                            if(plr.posZ > posAlignTargetZ)
                            {
                                if (Math.floor(plr.posZ - zTickSpeed) < posAlignTargetZ)
                                {
                                    posAlignStateSubstep = 4;
                                    return;
                                }

                                currentPressedKey = GLFW.GLFW_KEY_W;
                                System.out.println("MOVING FORWARD");
                            }
                            else
                            {
                                if (Math.floor(plr.posZ + zTickSpeed) > posAlignTargetZ)
                                {
                                    posAlignStateSubstep = 4;
                                    return;
                                }

                                currentPressedKey = GLFW.GLFW_KEY_S;
                                System.out.println("MOVING BACKWARD");
                            }

                            posAlignStateSubstep = 3;
                            pressCurrentKey();
                            break;
                        case 3:
                            if(zTickSpeed <= 0.01 || currentPressedKey == GLFW.GLFW_KEY_W
                                ? Math.floor(plr.posZ - zTickSpeed) < posAlignTargetZ
                                : Math.floor(plr.posZ + zTickSpeed) > posAlignTargetZ)
                            {
                                releaseCurrentKey();
                                posAlignStateSubstep = 4;
                                return;
                            }
                            break;
                        case 4:// ybegin
                            if(plr.posY > posAlignTargetY)
                            {
                                if (Math.floor(plr.posY - yTickSpeed) < posAlignTargetY)
                                {
                                    posAlignStateSubstep = 6;
                                    return;
                                }

                                currentPressedKey = GLFW.GLFW_KEY_LEFT_SHIFT;
                                System.out.println("MOVING DOWN");
                            }
                            else
                            {
                                if (Math.floor(plr.posY + yTickSpeed) > posAlignTargetY)
                                {
                                    posAlignStateSubstep = 6;
                                    return;
                                }

                                currentPressedKey = GLFW.GLFW_KEY_SPACE;
                                System.out.println("MOVING UP");
                            }

                            posAlignStateSubstep = 5;
                            pressCurrentKey();
                            break;
                        case 5:
                            if(yTickSpeed <= 0.1 || currentPressedKey == GLFW.GLFW_KEY_LEFT_SHIFT
                                ? Math.floor(plr.posY - yTickSpeed) < posAlignTargetY
                                : Math.floor(plr.posY + yTickSpeed) > posAlignTargetY)
                            {
                                releaseCurrentKey();
                                posAlignStateSubstep = 6;
                                return;
                            }
                            break;
                        case 6:
                            positionAlignQuery = false;
                            movingQuery = true;
                            break;
                    }
                    break;
           }
        }
        else if(movingQuery)
        {
            EntityPlayerSP plr = Minecraft.getInstance().player;

            switch(movingState)
            {
                case NONE:
                    System.out.println("moving query NONE");
                    if(Math.abs(plr.posX) == Math.abs(movingTarget.x))
                    {
                        if(Math.abs(plr.posZ) == Math.abs(movingTarget.z))
                        {
                            if(Math.abs(plr.posY) == Math.abs(movingTarget.y))
                            {
                                System.out.println("moving query ENDED inside NONE");
                                movingState = STATE.ENDED;
                            }
                            else
                            {
                                System.out.println("moving query pressed Y");

                                movingState       = STATE.Y;
                                currentPressedKey = movingDir.y < 0 ? GLFW.GLFW_KEY_LEFT_SHIFT : GLFW.GLFW_KEY_SPACE;

                                pressCurrentKey();
                            }
                        }
                        else
                        {
                            System.out.println("moving query pressed Z");
                            movingState       = STATE.Z;
                            currentPressedKey = movingDir.z < 0 ? GLFW.GLFW_KEY_W : GLFW.GLFW_KEY_S;

                            pressCurrentKey();
                        }
                    }
                    else
                    {
                        System.out.println("moving query pressed X");
                        movingState       = STATE.X;
                        currentPressedKey = movingDir.x < 0 ? GLFW.GLFW_KEY_A : GLFW.GLFW_KEY_D;

                        pressCurrentKey();
                    }
                    break;
                case X:
                    System.out.println("moving query X");
                    if(Math.floor(Math.abs(plr.posX)) == Math.abs(movingTarget.x))
                    {
                        releaseCurrentKey();
                        System.out.println("moving query released");

                        if(Math.abs(plr.posZ) == Math.abs(movingTarget.z))
                        {
                            if(Math.abs(plr.posY) == Math.abs(movingTarget.y))
                            {
                                movingState = STATE.ENDED;
                            }
                            else
                            {
                                movingState       = STATE.Y;
                                currentPressedKey = movingDir.y < 0 ? GLFW.GLFW_KEY_LEFT_SHIFT : GLFW.GLFW_KEY_SPACE;

                                pressCurrentKey();
                            }
                        }
                        else
                        {
                            movingState       = STATE.Z;
                            currentPressedKey = movingDir.z < 0 ? GLFW.GLFW_KEY_W : GLFW.GLFW_KEY_S;

                            pressCurrentKey();
                        }
                    }
                    break;
                case Z:
                    System.out.println("moving query Z");
                    if(Math.floor(Math.abs(plr.posZ)) == Math.abs(movingTarget.z))
                    {
                        releaseCurrentKey();
                        System.out.println("moving query released");

                        if(Math.abs(plr.posY) == Math.abs(movingTarget.y))
                        {
                            movingState = STATE.ENDED;
                        }
                        else
                        {
                            movingState       = STATE.Y;
                            currentPressedKey = movingDir.y < 0 ? GLFW.GLFW_KEY_LEFT_SHIFT : GLFW.GLFW_KEY_SPACE;

                            pressCurrentKey();
                        }
                    }
                    break;
                case Y:
                    System.out.println("moving query Y");
                    if(Math.floor(Math.abs(plr.posY)) == Math.abs(movingTarget.y))
                    {
                        releaseCurrentKey();
                        System.out.println("moving query released");

                        movingState = STATE.ENDED;
                    }
                    break;
                case ENDED:
                    System.out.println("moving query ENDED");
                    movingQuery = false;
                    break;
            }
        }
    }

    private void setKeyState(int key, int action)
    {
        if(key == 0)
            return;

        mc.keyboardListener.onKeyEvent(
                mc.mainWindow.getHandle(),
                key,
                GLFW.glfwGetKeyScancode(key),
                action,
                0
        );
    }

    private void pressCurrentKey()
    {
        System.out.println("Pressed "+ currentPressedKey +": "+ GLFW.glfwGetKeyName(currentPressedKey, GLFW.glfwGetKeyScancode(currentPressedKey)));
        setKeyState(currentPressedKey, GLFW.GLFW_PRESS);
    }

    private void releaseCurrentKey()
    {
        setKeyState(currentPressedKey, GLFW.GLFW_RELEASE);
        currentPressedKey = 0;
    }
}
