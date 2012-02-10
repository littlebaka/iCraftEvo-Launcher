package com.kokakiwi.mclauncher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.kokakiwi.mclauncher.core.Launcher;
import com.kokakiwi.mclauncher.graphics.LoginForm;
import com.kokakiwi.mclauncher.utils.Configuration;
import com.kokakiwi.mclauncher.utils.LocalString;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.ProfileManager;
import com.kokakiwi.mclauncher.utils.java.StringFormatter;
import com.kokakiwi.mclauncher.utils.java.SystemUtils;
import com.kokakiwi.mclauncher.utils.java.Utils;
import com.kokakiwi.mclauncher.utils.java.Version;

public class LauncherFrame extends Frame
{
    private static final long serialVersionUID = -439450888759860507L;
    
    public static Version     APP_VERSION      = new Version(0, 9, 5, 1);
    
    public ProfileManager     profiles         = new ProfileManager();
    
    public JPanel             panel;
    public LoginForm          loginForm;
    public Launcher           launcher;
    public LocalString        locale;
    
    public LauncherFrame()
    {
        super();
        instance = this;
        
        MCLogger.info("Starting MCLauncher [" + APP_VERSION + "]...");
        MCLogger.printSystemInfos();
        
        if (getConfig().getBoolean("launcher.autoConnectServer.connect"))
        {
            getConfig().set("server",
                    getConfig().getString("launcher.autoConnectServer.ip"));
            getConfig().set("port",
                    getConfig().getString("launcher.autoConnectServer.port"));
        }
        
        locale = new LocalString(this, getConfig().getStringList(
                "launcher.langs"));
        
        setTitle(getConfig().getString("launcher.windowTitle"));
        if (SystemUtils.getSystemOS() == SystemUtils.OS.macosx)
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty(
                    "com.apple.mrj.application.apple.menu.about.name",
                    getConfig().getString("launcher.windowTitle"));
        }
        setBackground(Color.BLACK);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(854, 480));
        
        loginForm = new LoginForm(this);
        panel.add(loginForm);
        
        setLayout(new BorderLayout());
        add(panel, "Center");
        
        pack();
        setLocationRelativeTo(null);
        
        try
        {
            setIconImage(ImageIO.read(Utils
                    .getResourceAsStream("res/favicon.png")));
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        
        addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent paramWindowEvent)
            {
                new Thread() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(30000L);
                        }
                        catch (final InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        System.out.println("FORCING EXIT!");
                        System.exit(0);
                    }
                }.start();
                System.exit(0);
            }
            
        });
    }
    
    public Configuration getConfig()
    {
        return profiles.getCurrentProfile().getConfig();
    }
    
    @SuppressWarnings("deprecation")
    public void login()
    {
        final boolean offlineMode = getConfig().getBoolean(
                "launcher.offlineMode");
        
        if (offlineMode)
        {
            playOffline();
        }
        else
        {
            try
            {
                loginForm.setStatusText(locale.getString("login.loggingIn"));
                MCLogger.info("Logging in...");
                final Map<String, String> keys = new HashMap<String, String>();
                keys.put("USERNAME",
                        URLEncoder.encode(loginForm.getUserName(), "UTF-8"));
                keys.put("PASSWORD",
                        URLEncoder.encode(new String(loginForm.getPassword())));
                final String parameters = StringFormatter.format(getConfig()
                        .getString("launcher.loginParameters"), keys);
                
                final String loginURL = getConfig().getBoolean(
                        "launcher.httpsMode") ? getConfig().getString(
                        "launcher.loginURLHTTPS") : getConfig().getString(
                        "launcher.loginURLHTTP");
                        
                MCLogger.debug("Logging in using URL '" + loginURL + "'");
                
                final String result = Utils.executePost(loginURL, parameters,
                        getConfig().getString("updater.keyFileName"));
                
                MCLogger.debug("Result: " + result);
                if (result == null)
                {
                    loginForm.askOfflineMode();
                    loginForm.setStatusText(locale
                            .getString("launcher.loginError"));
                    return;
                }
                if (!result.contains(":"))
                {
                    if (result.trim().equals("Bad login"))
                    {
                        loginForm.setStatusText(locale
                                .getString("launcher.badLogin"));
                    }
                    else if (result.trim().equals("Old version"))
                    {
                        loginForm.setStatusText(locale
                                .getString("launcher.oldVersion"));
                    }
                    else
                    {
                        loginForm.setStatusText(result);
                    }
                    return;
                }
                final String[] values = result.split(":");
                getConfig().set("latestVersion", values[0].trim());
                getConfig().set("downloadTicket", values[1].trim());
                getConfig().set("userName", values[2].trim());
                getConfig().set("sessionID", values[3].trim());
                MCLogger.info("Login successful.");
                MCLogger.debug("Session ID: " + values[3].trim());
                loginForm.loginOk();
                
                runGame();
                
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void playOffline()
    {
        getConfig().set("latestVersion", "-1");
        getConfig().set("userName", loginForm.getUserName());
        getConfig().set("sessionID", "43546534723277");
        loginForm.loginOk();
        runGame();
    }
    
    public void doLogin()
    {
        MCLogger.debug("doLogin");
        new Thread() {
            @Override
            public void run()
            {
                LauncherFrame.this.login();
            }
        }.start();
    }
    
    public void loginError()
    {
        
    }
    
    public void runGame()
    {
        launcher = new Launcher(this);
        launcher.init();
        
        removeAll();
        add(launcher, "Center");
        validate();
        
        launcher.start();
        
        setTitle(getConfig().getString("gameLauncher.gameName"));
    }
    
    public void refresh()
    {
        if (getConfig().getBoolean("launcher.autoConnectServer.connect"))
        {
            getConfig().set("server",
                    getConfig().getString("launcher.autoConnectServer.ip"));
            getConfig().set("port",
                    getConfig().getString("launcher.autoConnectServer.port"));
        }
        else
        {
            getConfig().set("server", null);
            getConfig().set("port", null);
        }
        
        locale = new LocalString(this, getConfig().getStringList(
                "launcher.langs"));
        setTitle(getConfig().getString("launcher.windowTitle"));
    }
    
    private static LauncherFrame instance;
    
    public static LauncherFrame getInstance()
    {
        return instance;
    }
    
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (final Exception e)
        {
        }
        final LauncherFrame launcherFrame = new LauncherFrame();
        launcherFrame.setVisible(true);
        launcherFrame.getConfig().set("stand-alone", "true");
        if (args.length >= 1)
        {
            launcherFrame.loginForm.userName.setText(args[0]);
            if (args.length >= 2)
            {
                launcherFrame.loginForm.password.setText(args[1]);
                launcherFrame.doLogin();
                if (args.length >= 3)
                {
                    String ip = args[2];
                    String port = "25565";
                    if (ip.contains(":"))
                    {
                        final String[] parts = ip.split(":");
                        ip = parts[0];
                        port = parts[1];
                    }
                    
                    launcherFrame.getConfig().set("server", ip);
                    launcherFrame.getConfig().set("port", port);
                }
            }
        }
    }
}
