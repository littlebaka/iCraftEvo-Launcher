package com.kokakiwi.mclauncher.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.graphics.utils.LogoPanel;
import com.kokakiwi.mclauncher.graphics.utils.TexturedPanel;
import com.kokakiwi.mclauncher.graphics.utils.TransparentButton;
import com.kokakiwi.mclauncher.graphics.utils.TransparentCheckbox;
import com.kokakiwi.mclauncher.graphics.utils.TransparentLabel;
import com.kokakiwi.mclauncher.graphics.utils.TransparentPanel;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.java.ClassesUtils;
import com.kokakiwi.mclauncher.utils.java.Utils;

public class LoginForm extends JPanel
{
    private static final long         serialVersionUID = -2684390357579600827L;
    
    private final LauncherFrame       launcherFrame;
    
    private JScrollPane               scrollPane       = null;
    public JTextField                 userName         = new JTextField(20);
    public JPasswordField             password         = new JPasswordField(20);
    private JPanel                    loginBox;
    private final TransparentCheckbox rememberBox;
    private final TransparentButton   launchButton;
    private final TransparentButton   optionsButton;
    private final TransparentButton   retryButton;
    private final TransparentButton   offlineButton;
    private final TransparentLabel    statusText       = new TransparentLabel(
                                                               "", 0);
    private final JPanel              southPanel       = new TexturedPanel(
                                                               "res/dirt.png");
    
    public LoginForm(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
        
        setLayout(new BorderLayout());
        
        rememberBox = new TransparentCheckbox(
                launcherFrame.locale.getString("login.rememberBox"));
        launchButton = new TransparentButton(
                launcherFrame.locale.getString("login.launchButton"));
        optionsButton = new TransparentButton(
                launcherFrame.locale.getString("login.optionsButton"));
        retryButton = new TransparentButton(
                launcherFrame.locale.getString("login.retryButton"));
        offlineButton = new TransparentButton(
                launcherFrame.locale.getString("login.offlineButton"));
        
        launchButton.addActionListener(new ClassesUtils.LaunchActionListener(
                launcherFrame));
        userName.addActionListener(new ClassesUtils.LaunchActionListener(
                launcherFrame));
        password.addActionListener(new ClassesUtils.LaunchActionListener(
                launcherFrame));
        retryButton.addActionListener(new ClassesUtils.TryAgainActionListener(
                launcherFrame));
        offlineButton
                .addActionListener(new ClassesUtils.PlayOfflineActionListener(
                        launcherFrame));
        optionsButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent paramActionEvent)
            {
                new OptionsPanel(LoginForm.this.launcherFrame).setVisible(true);
            }
        });
        
        readUsername();
        
        add(buildMainLoginPanel(), "Center");
    }
    
    public void refresh()
    {
        rememberBox
                .setText(launcherFrame.locale.getString("login.rememberBox"));
        launchButton.setText(launcherFrame.locale
                .getString("login.launchButton"));
        optionsButton.setText(launcherFrame.locale
                .getString("login.optionsButton"));
        retryButton
                .setText(launcherFrame.locale.getString("login.retryButton"));
        offlineButton.setText(launcherFrame.locale
                .getString("login.offlineButton"));
        userName = new JTextField(20);
        password = new JPasswordField(20);
        
        removeAll();
        southPanel.removeAll();
        setLayout(new BorderLayout());
        readUsername();
        add(buildMainLoginPanel(), "Center");
        validate();
    }
    
    private JPanel buildMainLoginPanel()
    {
        final JPanel panel = new TransparentPanel(new BorderLayout());
        
        if (System.getenv("debugMode") == null)
        {
            panel.add(getUpdateNews(), "Center");
        }
        
        southPanel.setLayout(new BorderLayout());
        southPanel.add(new LogoPanel(), "West");
        southPanel.add(statusText, "Center");
        loginBox = buildLoginPanel();
        southPanel.add(center(loginBox), "East");
        southPanel.setPreferredSize(new Dimension(100, 100));
        
        panel.add(southPanel, "South");
        
        return panel;
    }
    
    private JPanel buildLoginPanel()
    {
        final TransparentPanel panel = new TransparentPanel();
        final BorderLayout layout = new BorderLayout();
        layout.setHgap(0);
        layout.setVgap(8);
        panel.setLayout(layout);
        final GridLayout gl1 = new GridLayout(0, 1);
        gl1.setVgap(2);
        final GridLayout gl2 = new GridLayout(0, 1);
        gl2.setVgap(2);
        final GridLayout gl3 = new GridLayout(0, 1);
        gl3.setVgap(2);
        
        final TransparentPanel titles = new TransparentPanel(gl1);
        final TransparentPanel values = new TransparentPanel(gl2);
        
        titles.add(new TransparentLabel(launcherFrame.locale
                .getString("login.userLabel") + ":", 4));
        titles.add(new TransparentLabel(launcherFrame.locale
                .getString("login.passwordLabel") + ":", 4));
        titles.add(new TransparentLabel("", 4));
        
        values.add(userName);
        values.add(password);
        values.add(rememberBox);
        
        panel.add(titles, "West");
        panel.add(values, "Center");
        
        final TransparentPanel loginPanel = new TransparentPanel(
                new BorderLayout());
        
        final TransparentPanel third = new TransparentPanel(gl3);
        third.add(optionsButton);
        third.add(launchButton);
        third.add(new TransparentPanel());
        
        third.setInsets(0, 10, 0, 10);
        titles.setInsets(0, 0, 0, 4);
        
        loginPanel.add(third, "Center");
        
        panel.add(loginPanel, "East");
        
        return panel;
    }
    
    private JPanel getUpdateNews()
    {
        if (scrollPane == null)
        {
            final JTextPane editorPane = new JTextPane();
            try
            {
                editorPane
                        .setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center>"
                                + launcherFrame.locale
                                        .getString("launcher.loadBrowser")
                                + "</center></font></body></html>");
                editorPane.addHyperlinkListener(new HyperlinkListener() {
                    
                    public void hyperlinkUpdate(HyperlinkEvent he)
                    {
                        if (he.getEventType() == EventType.ACTIVATED)
                        {
                            try
                            {
                                editorPane.setPage(he.getURL());
                            }
                            catch (final IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                
                editorPane.setBackground(Color.DARK_GRAY);
                editorPane.setEditable(false);
                scrollPane = new JScrollPane(editorPane);
                scrollPane.setBorder(null);
                editorPane.setMargin(null);
                
                scrollPane.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));
                
                new ClassesUtils.BrowserThread(editorPane, launcherFrame
                        .getConfig().getString("launcher.browserHomeURL"))
                        .start();
            }
            catch (final Exception e)
            {
                MCLogger.error(e.getLocalizedMessage());
            }
        }
        
        final JPanel panel = new TexturedPanel("res/stone.png");
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, "Center");
        
        return panel;
    }
    
    public void askOfflineMode()
    {
        southPanel.removeAll();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(new LogoPanel(), "West");
        southPanel.add(statusText, "Center");
        southPanel.add(center(buildOfflineLoginPanel()), "East");
        southPanel.setPreferredSize(new Dimension(100, 100));
        southPanel.validate();
    }
    
    public void refreshLoginBox()
    {
        southPanel.removeAll();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(new LogoPanel(), "West");
        southPanel.add(statusText, "Center");
        southPanel.add(center(buildLoginPanel()), "East");
        southPanel.setPreferredSize(new Dimension(100, 100));
        southPanel.validate();
    }
    
    private JPanel buildOfflineLoginPanel()
    {
        final TransparentPanel panel = new TransparentPanel();
        final BorderLayout layout = new BorderLayout();
        layout.setHgap(0);
        layout.setVgap(8);
        panel.setLayout(layout);
        final GridLayout gl1 = new GridLayout(0, 1);
        gl1.setVgap(2);
        final GridLayout gl2 = new GridLayout(0, 1);
        gl2.setVgap(2);
        final GridLayout gl3 = new GridLayout(0, 1);
        gl3.setVgap(2);
        
        final TransparentPanel titles = new TransparentPanel(gl1);
        final TransparentPanel values = new TransparentPanel(gl2);
        
        titles.add(new TransparentLabel(launcherFrame.locale
                .getString("login.userLabel") + ":", 4));
        titles.add(new TransparentLabel(launcherFrame.locale
                .getString("login.passwordLabel") + ":", 4));
        titles.add(new TransparentLabel("", 4));
        
        values.add(userName);
        values.add(password);
        values.add(rememberBox);
        
        panel.add(titles, "West");
        panel.add(values, "Center");
        
        final TransparentPanel loginPanel = new TransparentPanel(
                new BorderLayout());
        
        final TransparentPanel third = new TransparentPanel(gl3);
        third.add(offlineButton);
        third.add(retryButton);
        third.add(new TransparentPanel());
        
        third.setInsets(0, 10, 0, 10);
        titles.setInsets(0, 0, 0, 4);
        
        loginPanel.add(third, "Center");
        
        panel.add(loginPanel, "East");
        
        return panel;
    }
    
    public void loginOk()
    {
        writeUsername();
    }
    
    //Ceci est la fonction qui lit le fichier lastlogin ;)
    private void readUsername()
    {
        try
        {
            final File lastLogin = new File(
                    Utils.getWorkingDirectory(launcherFrame), "lastlogin");
            
            if (lastLogin.exists())
            {
                final Cipher cipher = getCipher(
                        Cipher.DECRYPT_MODE,
                        launcherFrame.getConfig().getString(
                                "updater.loginFileEncryptionKey"));
                DataInputStream dis;
                if (cipher != null)
                {
                    dis = new DataInputStream(new CipherInputStream(
                            new FileInputStream(lastLogin), cipher));
                }
                else
                {
                    dis = new DataInputStream(new FileInputStream(lastLogin));
                }
                userName.setText(dis.readUTF());
                password.setText(dis.readUTF());
                rememberBox.setSelected(password.getPassword().length > 0);
                dis.close();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void writeUsername()
    {
        try
        {
            final File lastLogin = new File(
                    Utils.getWorkingDirectory(launcherFrame), "lastlogin");
            
            final Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, launcherFrame
                    .getConfig().getString("updater.loginFileEncryptionKey"));
            DataOutputStream dos;
            if (cipher != null)
            {
                dos = new DataOutputStream(new CipherOutputStream(
                        new FileOutputStream(lastLogin), cipher));
            }
            else
            {
                dos = new DataOutputStream(new FileOutputStream(lastLogin));
            }
            dos.writeUTF(userName.getText());
            dos.writeUTF(rememberBox.isSelected() ? new String(password
                    .getPassword()) : "");
            dos.close();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private Cipher getCipher(int mode, String password) throws Exception
    {
        final Random random = new Random(43287234L);
        final byte[] salt = new byte[8];
        random.nextBytes(salt);
        final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
        
        final SecretKey pbeKey = SecretKeyFactory.getInstance(
                "PBEWithMD5AndDES").generateSecret(
                new PBEKeySpec(password.toCharArray()));
        final Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }
    
    private Component center(Component c)
    {
        final TransparentPanel tp = new TransparentPanel(new GridBagLayout());
        tp.add(c);
        return tp;
    }
    
    public String getUserName()
    {
        final String user = userName.getText() != null
                && userName.getText() != "" ? userName.getText() : null;
        return user;
    }
    
    public char[] getPassword()
    {
        return password.getPassword();
    }
    
    public void setStatusText(String message)
    {
        statusText.setText(message);
    }
}
