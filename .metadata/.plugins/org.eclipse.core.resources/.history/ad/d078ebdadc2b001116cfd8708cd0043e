package com.kokakiwi.mclauncher.utils;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.kokakiwi.mclauncher.LauncherFrame;

@SuppressWarnings("rawtypes")
public class ProfileListModel implements ListModel
{
    private final LauncherFrame launcherFrame;
    
    public ProfileListModel(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
    }
    
    public void addListDataListener(ListDataListener l)
    {
        
    }
    
    public Object getElementAt(int index)
    {
        return launcherFrame.profiles.getProfiles().values().toArray()[index];
    }
    
    public int getSize()
    {
        if (launcherFrame != null)
        {
            return launcherFrame.profiles.getProfiles().size();
        }
        return 0;
    }
    
    public void removeListDataListener(ListDataListener l)
    {
        
    }
    
}
