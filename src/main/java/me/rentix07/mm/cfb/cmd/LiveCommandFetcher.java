package me.rentix07.mm.cfb.cmd;/*
 * Created on 29.12.2019 10:19
 * by Pawel
 */

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public
class LiveCommandFetcher
{
    private
    Map<String, ILiveCommandExecutor> commandExecutorMap;

    private StringBuilder fetchStream;
    private boolean fetchStreamActive;

    public
    LiveCommandFetcher()
    {
        commandExecutorMap = new HashMap<>();
        fetchStream = new StringBuilder();
        fetchStreamActive = false;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void setExecutor(String cmdName, ILiveCommandExecutor executor)
    {
        commandExecutorMap.put(cmdName, executor);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onChatEvent(GuiScreenEvent.KeyboardKeyPressedEvent event)
    {
        if(event.getKeyCode() == GLFW.GLFW_KEY_BACKSPACE)
        {
            if (fetchStreamActive)
            {
                if(fetchStream.length() > 0)
                    fetchStream.deleteCharAt(fetchStream.length() - 1);
                else
                    fetchStreamActive = false;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onChatEvent(GuiScreenEvent.KeyboardCharTypedEvent event)
    {
        char contextChar = event.getCodePoint();

        if(!fetchStreamActive)
        {
            if(contextChar == ';')
            {
                fetchStreamActive = true;
            }
        }
        else if(contextChar == ';')
        {
            String cmd = fetchStream.toString();
            String cmdName = retrieveCommandName(cmd);

            System.out.println("[LiveCommand] Fetched as '"+ cmdName +"': "+ cmd);

            ILiveCommandExecutor cmdExecutor = commandExecutorMap.get(cmdName);
            if(cmdExecutor != null)
            {
                String[] args = retrieveCommandArguments(cmd);

                StringBuilder stringBuilder = new StringBuilder();
                for(String arg : args)
                {
                    stringBuilder.append('\'');
                    stringBuilder.append(arg);
                    stringBuilder.append('\'');
                    stringBuilder.append(", ");
                }

                System.out.println("[LiveCommand] and args: "+ stringBuilder.toString());

                escapeInGameCommandLine();

                cmdExecutor.onCommand(args);
            }

            fetchStreamActive = false;
            fetchStream = new StringBuilder();
        }
        else fetchStream.append(contextChar);
    }

    private void escapeInGameCommandLine()
    {
        Minecraft mc = Minecraft.getInstance();
        mc.keyboardListener.onKeyEvent(
                mc.mainWindow.getHandle(),
                GLFW.GLFW_KEY_ESCAPE,
                GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_ESCAPE),
                GLFW.GLFW_PRESS,
                0
        );
        mc.keyboardListener.onKeyEvent(
                mc.mainWindow.getHandle(),
                GLFW.GLFW_KEY_ESCAPE,
                GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_ESCAPE),
                GLFW.GLFW_RELEASE,
                0
        );
    }

    private String retrieveCommandName(String cmd)
    {
        StringBuilder name = new StringBuilder();

        for(int i=-1, sz=cmd.length(); ++i < sz;)
        {
            char contextChar = cmd.charAt(i);

            if(contextChar == ' ')
                break;
            else
            {
                name.append(contextChar);
            }
        }

        return name.toString();
    }

    private String[] retrieveCommandArguments(String cmd)
    {
        List<String> args = new ArrayList<>();

        int itrAt = -1;
        int cmdLen = cmd.length();

        int foundArgsCount = 0;

        do
        {
            StringBuilder arg = new StringBuilder();

            for(; ++itrAt < cmdLen;)
            {
                char contextChar = cmd.charAt(itrAt);

                if(contextChar == ' ')
                {
                    if(arg.length() > 0)
                    {
                        // exclude command name by the condition
                        if(++foundArgsCount > 1)
                            args.add(arg.toString());
                    }

                    break;
                }
                else arg.append(contextChar);
            }

            if(itrAt == cmdLen)
            {
                if(++foundArgsCount > 1 && arg.length() > 0)
                {
                    args.add(arg.toString());
                }
                break;
            }
        }
        while (true);

        return args.toArray(new String[0]);
    }
}
