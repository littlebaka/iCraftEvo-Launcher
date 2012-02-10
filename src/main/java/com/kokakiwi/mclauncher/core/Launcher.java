package com.kokakiwi.mclauncher.core;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.java.Utils;

public class Launcher extends Applet implements Runnable, AppletStub,
        MouseListener
{
    private static final long   serialVersionUID = -2433230602156426362L;
    
    private final LauncherFrame launcherFrame;
    public Applet               applet           = null;
    private Image               bgImage;
    private int                 context          = 0;
    private boolean             active           = false;
    private VolatileImage       img;
    public GameUpdater          updater;
    public GameLauncher         launcher;
    public boolean              pauseAskUpdate   = false;
    private int                 percentage;
    private State               state            = State.INIT;
    private boolean             hasMouseListener;
    public String               subtaskMessage   = "";
    public Map<String, String>  customParameters = new HashMap<String, String>();
    
    public Launcher(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
    }
    
    @Override
    public void init()
    {
        if (applet != null)
        {
            applet.init();
            return;
        }
        
        try
        {
            bgImage = ImageIO.read(Utils.getResourceAsStream("res/dirt.png"))
                    .getScaledInstance(32, 32, 16);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        
        if (launcherFrame.getConfig().getString("server") != null)
        {
            customParameters.put("server",
                    launcherFrame.getConfig().getString("server"));
        }
        
        if (launcherFrame.getConfig().getString("port") != null)
        {
            customParameters.put("port",
                    launcherFrame.getConfig().getString("port"));
        }
        
        if (launcherFrame.getConfig().getString("latestVersion") != null)
        {
            customParameters.put("latestVersion", launcherFrame.getConfig()
                    .getString("latestVersion"));
        }
        
        if (launcherFrame.getConfig().getString("downloadTicket") != null)
        {
            customParameters.put("downloadTicket", launcherFrame.getConfig()
                    .getString("downloadTicket"));
        }
        
        if (launcherFrame.getConfig().getString("sessionID") != null)
        {
            customParameters.put("sessionid", launcherFrame.getConfig()
                    .getString("sessionID"));
        }
        
        customParameters
                .put("username", launcherFrame.getConfig()
                        .getString("userName") == null ? "Player"
                        : launcherFrame.getConfig().getString("userName"));
        
        System.out.println(launcherFrame.getConfig().get("sessionID"));
        System.out.println(customParameters.get("sessionID"));
        
        // this.customParameters.put("stand-alone", "true");
        
        updater = new GameUpdater(launcherFrame);
        launcher = new GameLauncher(launcherFrame);
    }
    
    public void mouseClicked(MouseEvent paramMouseEvent)
    {
        
    }
    
    public void mousePressed(MouseEvent me)
    {
        final int x = me.getX() / 2;
        final int y = me.getY() / 2;
        final int w = getWidth() / 2;
        final int h = getHeight() / 2;
        
        if (contains(x, y, w / 2 - 56 - 8, h / 2, 56, 20))
        {
            removeMouseListener(this);
            updater.shouldUpdate = true;
            pauseAskUpdate = false;
            hasMouseListener = false;
        }
        if (contains(x, y, w / 2 + 8, h / 2, 56, 20))
        {
            removeMouseListener(this);
            updater.shouldUpdate = false;
            pauseAskUpdate = false;
            hasMouseListener = false;
        }
    }
    
    public void mouseReleased(MouseEvent paramMouseEvent)
    {
        
    }
    
    public void mouseEntered(MouseEvent paramMouseEvent)
    {
        
    }
    
    public void mouseExited(MouseEvent paramMouseEvent)
    {
        
    }
    
    public void appletResize(int paramInt1, int paramInt2)
    {
        
    }
    
    public void run()
    {
    }
    
    @Override
    public void start()
    {
        if (applet != null)
        {
            applet.start();
            return;
        }
        
        // Game Launch
        Thread t = new Thread() {
            @Override
            public void run()
            {
                updater.run();
                launcher.run();
            }
        };
        t.setDaemon(true);
        t.start();
        
        // Launcher Graphic Update
        t = new Thread() {
            @Override
            public void run()
            {
                while (applet == null)
                {
                    Launcher.this.repaint();
                    try
                    {
                        Thread.sleep(10L);
                    }
                    catch (final InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    @Override
    public void stop()
    {
        if (applet != null)
        {
            active = false;
            applet.stop();
            return;
        }
    }
    
    @Override
    public void destroy()
    {
        if (applet != null)
        {
            applet.destroy();
            return;
        }
    }
    
    public void replace(Applet applet)
    {
        this.applet = applet;
        applet.setStub(this);
        applet.setSize(getWidth(), getHeight());
        
        setLayout(new BorderLayout());
        add(applet, "Center");
        
        applet.init();
        active = true;
        applet.start();
        validate();
        
        launcherFrame.setTitle(launcherFrame.getConfig().getString(
                "gameLauncher.gameName"));
    }
    
    @Override
    public void paint(Graphics g2)
    {
        if (applet != null)
        {
            return;
        }
        
        final int w = getWidth() / 2;
        final int h = getHeight() / 2;
        if (img == null || img.getWidth() != w || img.getHeight() != h)
        {
            img = createVolatileImage(w, h);
        }
        
        final Graphics g = img.getGraphics();
        for (int x = 0; x <= w / 32; x++)
        {
            for (int y = 0; y <= h / 32; y++)
            {
                g.drawImage(bgImage, x * 32, y * 32, null);
            }
        }
        if (pauseAskUpdate)
        {
            if (!hasMouseListener)
            {
                hasMouseListener = true;
                addMouseListener(this);
            }
            g.setColor(Color.LIGHT_GRAY);
            String msg = launcherFrame.locale
                    .getString("updater.newUpdateAvailable", new String[][]{{"NEWVERSION", updater.latestVersionToUpdate}});
            g.setFont(new Font(null, 1, 20));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
                    h / 2 - fm.getHeight() * 2);
            
            g.setFont(new Font(null, 0, 12));
            fm = g.getFontMetrics();
            
            g.fill3DRect(w / 2 - 56 - 8, h / 2, 56, 20, true);
            g.fill3DRect(w / 2 + 8, h / 2, 56, 20, true);
            
            msg = launcherFrame.locale.getString("updater.askUpdate");
            g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - 8);
            
            g.setColor(Color.BLACK);
            msg = launcherFrame.locale.getString("global.yesStr");
            g.drawString(msg, w / 2 - 56 - 8 - fm.stringWidth(msg) / 2 + 28,
                    h / 2 + 14);
            msg = launcherFrame.locale.getString("global.noStr");
            g.drawString(msg, w / 2 + 8 - fm.stringWidth(msg) / 2 + 28,
                    h / 2 + 14);
        }
        else
        {
            g.setColor(Color.LIGHT_GRAY);
            
            String msg = launcherFrame.locale.getString("updater.title");
            if (updater.fatalError)
            {
                msg = "Failed to launch";
            }
            
            g.setFont(new Font(null, 1, 20));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
                    h / 2 - fm.getHeight() * 2);
            
            g.setFont(new Font(null, 0, 12));
            fm = g.getFontMetrics();
            msg = getDescriptionForState();
            if (updater.fatalError)
            {
                msg = updater.fatalErrorDescription;
                subtaskMessage = "";
            }
            
            g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
                    h / 2 + fm.getHeight() * 1);
            msg = subtaskMessage;
            g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
                    h / 2 + fm.getHeight() * 2);
            
            if (!updater.fatalError)
            {
                g.setColor(Color.black);
                g.fillRect(64, h - 64, w - 128 + 1, 5);
                g.setColor(new Color(32768));
                g.fillRect(64, h - 64, percentage * (w - 128) / 100, 4);
                g.setColor(new Color(2138144));
                g.fillRect(65, h - 64 + 1, percentage * (w - 128) / 100 - 2, 1);
            }
        }
        
        g.dispose();
        
        g2.drawImage(img, 0, 0, w * 2, h * 2, null);
    }
    
    private String getDescriptionForState()
    {
        if (state.getDescription() != null)
        {
            return state.getDescription();
        }
        else
        {
            return launcherFrame.locale.getString("updater.states."
                    + state.name());
        }
    }
    
    @Override
    public void update(Graphics g)
    {
        paint(g);
    }
    
    private boolean contains(int x, int y, int xx, int yy, int w, int h)
    {
        return x >= xx && y >= yy && x < xx + w && y < yy + h;
    }
    
    @Override
    public String getParameter(String name)
    {
        String custom = customParameters.get(name);
        if (custom == null)
        {
            custom = launcherFrame.getConfig().getString(name);
            if(custom == null)
            {
                try
                {
                    custom = super.getParameter(name);
                }
                catch (final Exception e)
                {
                    customParameters.put(name, null);
                }
            }
        }
        
        MCLogger.debug("Asked '" + name + "' parameter = '" + custom + "'");
        return custom;
    }
    
    @Override
    public URL getDocumentBase()
    {
        try
        {
            return new URL(launcherFrame.getConfig().getString(
                    "gameLauncher.documentBaseURL"));
        }
        catch (final MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public boolean isActive()
    {
        if (context == 0)
        {
            context = -1;
            try
            {
                if (getAppletContext() != null)
                {
                    context = 1;
                }
            }
            catch (final Exception localException)
            {
            }
        }
        if (context == -1)
        {
            return active;
        }
        return super.isActive();
    }
    
    public void setPercentage(int percentage)
    {
        this.percentage = percentage;
    }
    
    public int getPercentage()
    {
        return percentage;
    }
    
    public void setState(int state)
    {
        this.state = State.values()[state - 1];
    }
    
    public void setState(State state)
    {
        this.state = state;
        MCLogger.info(this.getDescriptionForState());
    }
    
    public State getState()
    {
        return state;
    }
    
}
