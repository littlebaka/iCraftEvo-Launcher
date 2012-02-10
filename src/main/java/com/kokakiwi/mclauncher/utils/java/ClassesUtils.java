package com.kokakiwi.mclauncher.utils.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JTextPane;

import com.kokakiwi.mclauncher.LauncherFrame;

public class ClassesUtils
{
    
    public static class LaunchActionListener implements ActionListener
    {
        private final LauncherFrame launcherFrame;
        
        public LaunchActionListener(LauncherFrame launcherFrame)
        {
            this.launcherFrame = launcherFrame;
        }
        
        public void actionPerformed(ActionEvent paramActionEvent)
        {
            launcherFrame.doLogin();
        }
        
    }
    
    public static class TryAgainActionListener implements ActionListener
    {
        private final LauncherFrame launcherFrame;
        
        public TryAgainActionListener(LauncherFrame launcherFrame)
        {
            this.launcherFrame = launcherFrame;
        }
        
        public void actionPerformed(ActionEvent paramActionEvent)
        {
            launcherFrame.loginForm.refreshLoginBox();
        }
        
    }
    
    public static class PlayOfflineActionListener implements ActionListener
    {
        private final LauncherFrame launcherFrame;
        
        public PlayOfflineActionListener(LauncherFrame launcherFrame)
        {
            this.launcherFrame = launcherFrame;
        }
        
        public void actionPerformed(ActionEvent paramActionEvent)
        {
            launcherFrame.playOffline();
        }
        
    }
    
    public static class BrowserThread extends Thread
    {
        private final JTextPane editorPane;
        public String           url;
        
        public BrowserThread(JTextPane editorPane, String url)
        {
            this.editorPane = editorPane;
            this.url = url;
        }
        
        @Override
        public void run()
        {
            try
            {
                editorPane.setPage(new URL(url));
            }
            catch (final Exception e)
            {
                editorPane
                        .setText("<html><body>Error during loading page.</body></html>");
                e.printStackTrace();
            }
        }
    }
    
    public static class GameUpdaterThread extends Thread
    {
        public InputStream[] is;
        public URLConnection urlconnection;
        
        @Override
        public void run()
        {
            try
            {
                is[0] = urlconnection.getInputStream();
            }
            catch (final IOException localIOException)
            {
            }
        }
    }
    
}
