package me.rentix07.mm.cfb.evt;/*
 * Created on 28.12.2019 21:23
 * by Pawel
 */

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public
class PickupEventLst
{
    private int cancelGoingLeft = 0;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event)
    {
        if(cancelGoingLeft > 0 && (--cancelGoingLeft) == 0)
        {
            Minecraft mc = Minecraft.getInstance();
            mc.keyboardListener.onKeyEvent(
                    mc.mainWindow.getHandle(),
                    GLFW.GLFW_KEY_A,
                    GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_A),
                    GLFW.GLFW_RELEASE,
                    0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onPickupEvent(EntityItemPickupEvent event)
    {
        EntityPlayer plr = event.getEntityPlayer();
        plr.sendMessage(new TextComponentString("you have picked up something"));

        Minecraft  mc    = Minecraft.getInstance();


        EntityItem item      = event.getItem();
        ItemStack  itemStack = item.getItem();

        // ---
        try
        {
            List<Slot> slots   = mc.player.inventoryContainer.inventorySlots;
            boolean    matched = false;
            for (Slot slot : slots)
            {
                if (slot.isItemValid(itemStack))
                {
                    matched = true;
                }
            }

            if (matched)
            {
                plr.sendMessage(new TextComponentString("ITEM MATCHES"));
            }
            else
            {
                plr.sendMessage(new TextComponentString("ITEM DOES NOT MATCH"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // ---

        try
        {
            ITextComponent name = item.getName();
            assert name instanceof TextComponentTranslation;

            TextComponentTranslation name_cast = ((TextComponentTranslation)name);
            plr.sendMessage(new TextComponentString("cast + getKey(): "+ name_cast.getKey()));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        mc.keyboardListener.onKeyEvent(
                mc.mainWindow.getHandle(),
                GLFW.GLFW_KEY_A,
                GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_A),
                GLFW.GLFW_PRESS,
                0
        );

        cancelGoingLeft = itemStack.getCount();
        plr.sendMessage(new TextComponentString("Walking for "+ cancelGoingLeft +" ticks"));
    }
}
