package com.kokakiwi.mclauncher.core;

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.core.wrapper.Wrapper;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.java.Utils;

public class GameLauncher implements Runnable
{
    private final LauncherFrame launcherFrame;
    private final Launcher      launcher;
    private boolean             natives_loaded;
    public ClassLoader          classLoader;
    public Wrapper              wrapper;
    
    public GameLauncher(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
        launcher = launcherFrame.launcher;
    }
    
    public void run()
    {
        try
        {
            if (!launcherFrame.launcher.updater.fatalError)
            {
                final String path = Utils.getWorkingDirectory(launcherFrame)
                        + File.separator + "bin" + File.separator;
                final File dir = new File(path);
                updateClassPath(dir);
                runGame();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void updateClassPath(File dir)
    {
        launcher.setState(State.UPDATING_CLASSPATH);
        launcher.setPercentage(95);
        
        final int urlNumber = launcher.updater.getJarURLs().length;
        final URL[] urls = new URL[urlNumber];
        for (int i = 0; i < urlNumber; i++)
        {
            try
            {
                final String fileName = launcher.updater
                        .getJarName(launcher.updater.getJarURLs()[i]);
                urls[i] = new File(dir, fileName).toURI().toURL();
                MCLogger.info("Adding " + urls[i].getFile() + " to Classpath.");
            }
            catch (final MalformedURLException e)
            {
                MCLogger.error(e.getLocalizedMessage());
            }
        }
        
        if (classLoader == null)
        {
            classLoader = new URLClassLoader(urls);
        }
        
        String path = dir.getAbsolutePath();
        if (!path.endsWith(File.separator))
        {
            path = path + File.separator;
        }
        
        unloadNatives(path);
        
        System.setProperty("org.lwjgl.librarypath", path + "natives");
        System.setProperty("net.java.games.input.librarypath", path + "natives");
        natives_loaded = true;
    }
    
    private void unloadNatives(String nativePath)
    {
        if (!natives_loaded)
        {
            return;
        }
        try
        {
            final Field field = ClassLoader.class
                    .getDeclaredField("loadedLibraryNames");
            field.setAccessible(true);
            final Vector<?> libs = (Vector<?>) field.get(getClass()
                    .getClassLoader());
            final String path = new File(nativePath).getCanonicalPath();
            
            for (int i = 0; i < libs.size(); i++)
            {
                final String s = (String) libs.get(i);
                
                if (s.startsWith(path))
                {
                    libs.remove(i);
                    i--;
                }
            }
            
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void runGame()
    {
        launcher.setState(State.DONE);
        launcher.setPercentage(100);
        
        wrapper = new Wrapper(launcherFrame);
        wrapper.init();
        if (wrapper.createApplet())
        {
            MCLogger.info("Start game.");
            
            if (System.getenv("debugMode") == null || true)
            {
                launcher.replace(wrapper.getApplet());
            }
        }
    }
    
    public Applet createApplet() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException
    {
        final Class<?> appletClass = classLoader
                .loadClass("net.minecraft.client.MinecraftApplet");
        return (Applet) appletClass.newInstance();
    }
    
}
