package com.kokakiwi.mclauncher.core.wrapper;

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.java.Utils;

public class Wrapper
{
    private final LauncherFrame launcherFrame;
    private final ClassLoader   classLoader;
    
    private Applet              applet;
    
    private Executor worker = Executors.newCachedThreadPool();
    
    public Wrapper(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
        classLoader = launcherFrame.launcher.launcher.classLoader;
    }
    
    public void init()
    {
        // Make Minecraft portable ! :D
        final File workDirectory = Utils.getWorkingDirectory(launcherFrame);
        
        try
        {
            final List<Field> fields = JavaUtils.getFieldsWithType(
                    classLoader.loadClass("net.minecraft.client.Minecraft"),
                    File.class);
            for (final Field field : fields)
            {
                field.setAccessible(true);
                try
                {
                    field.get(classLoader
                            .loadClass("net.minecraft.client.Minecraft"));
                    field.set(null, workDirectory);
                }
                catch (final IllegalArgumentException e)
                {
                }
                catch (final IllegalAccessException e)
                {
                }
            }
        }
        catch (final ClassNotFoundException e)
        {
            MCLogger.info(e.getLocalizedMessage());
        }
    }
    
    public boolean createApplet()
    {
        try
        {
            applet = launcherFrame.launcher.launcher.createApplet();
            return true;
        }
        catch (final ClassNotFoundException e)
        {
            MCLogger.info(e.getLocalizedMessage());
        }
        catch (final InstantiationException e)
        {
            MCLogger.info(e.getLocalizedMessage());
        }
        catch (final IllegalAccessException e)
        {
            MCLogger.info(e.getLocalizedMessage());
        }
        return false;
    }
    
    public Applet getApplet()
    {
        return applet;
    }
}
