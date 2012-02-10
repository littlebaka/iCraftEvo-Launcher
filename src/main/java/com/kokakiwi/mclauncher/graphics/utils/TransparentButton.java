package com.kokakiwi.mclauncher.graphics.utils;

import javax.swing.JButton;

public class TransparentButton extends JButton
{
    private static final long serialVersionUID = -7363388629891733925L;
    
    public TransparentButton(String string)
    {
        super(string);
    }
    
    @Override
    public boolean isOpaque()
    {
        return false;
    }
}