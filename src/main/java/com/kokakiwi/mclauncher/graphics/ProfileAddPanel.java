package com.kokakiwi.mclauncher.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.kokakiwi.mclauncher.LauncherFrame;

public class ProfileAddPanel extends JDialog
{
    private static final long   serialVersionUID = 4912724109071891374L;
    
    private final LauncherFrame launcherFrame;
    
    private final JPanel        contentPanel     = new JPanel();
    private final JTextField    profileName;
    private Type                type;
    
    public ProfileAddPanel(LauncherFrame parent, final ProfilesPanel dialog,
            final Type type)
    {
        super(parent);
        setModal(true);
        launcherFrame = parent;
        this.type = type;
        setResizable(false);
        
        setBounds(100, 100, 302, 107);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        
        final JLabel lblName = new JLabel("Name :");
        lblName.setBounds(10, 11, 46, 14);
        contentPanel.add(lblName);
        
        profileName = new JTextField();
        profileName.setBounds(66, 8, 220, 20);
        contentPanel.add(profileName);
        profileName.setColumns(10);
        {
            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                final JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (profileName.getText().length() > 0)
                        {
                            switch (type)
                            {
                                case ADD:
                                    launcherFrame.profiles
                                            .createProfile(profileName
                                                    .getText());
                                    dialog.refreshList();
                                    break;
                                
                                case EDIT:
                                    dialog.getSelectedProfile().setName(
                                            profileName.getText());
                                    try
                                    {
                                        dialog.getSelectedProfile()
                                                .saveDescriptor();
                                    }
                                    catch (final Exception e1)
                                    {
                                        e1.printStackTrace();
                                    }
                                    dialog.refreshList();
                                    break;
                            }
                        }
                        ProfileAddPanel.this.setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                final JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        ProfileAddPanel.this.setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        
        setLocationRelativeTo(parent);
    }
    
    public Type getPType()
    {
        return type;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    
    @Override
    public void setName(String name)
    {
        profileName.setText(name);
    }
    
    public enum Type
    {
        ADD, EDIT;
    }
}
