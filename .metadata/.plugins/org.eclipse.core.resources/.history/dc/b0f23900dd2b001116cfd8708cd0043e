package com.kokakiwi.mclauncher.utils.java;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.core.wrapper.JavaUtils;
import com.kokakiwi.mclauncher.utils.MCLogger;

public class Utils
{
    public static File      workDir = null;
    public static JavaUtils JavaUtils;
    
    public static InputStream getResourceAsStream(String url)
    {
        return LauncherFrame.class.getResourceAsStream("/" + url);
    }
    
    public static File getWorkingDirectory(LauncherFrame launcherFrame)
    {
        return getWorkingDirectory(
                launcherFrame.getConfig().getString("updater.folderName"),
                launcherFrame.getConfig().getBoolean("updater.customGameDir") ? launcherFrame
                        .getConfig().getString("updater.gameDir") : null, true);
    }
    
    public static File getWorkingDirectory(String applicationName, String local, boolean saveLastDir)
    {
        if (workDir != null)
        {
            return workDir;
        }
        
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (SystemUtils.OS.values()[SystemUtils.getSystemOS().ordinal()])
        {
            case linux:
            case solaris:
                workingDirectory = new File(userHome,
                        '.' + applicationName + '/');
                break;
            case windows:
                final String applicationData = System.getenv("APPDATA");
                if (applicationData != null)
                {
                    workingDirectory = new File(applicationData, "."
                            + applicationName + '/');
                }
                else
                {
                    workingDirectory = new File(userHome,
                            '.' + applicationName + '/');
                }
                break;
            case macosx:
                workingDirectory = new File(userHome,
                        "Library/Application Support/" + applicationName);
                break;
            default:
                workingDirectory = new File(userHome, applicationName + '/');
        }
        
        if (local != null)
        {
            workingDirectory = new File(
                    new File(local + "/").getAbsoluteFile(), "."
                            + applicationName + "/");
        }
        
        if (!workingDirectory.exists() && !workingDirectory.mkdirs())
        {
            throw new RuntimeException(
                    "The working directory could not be created: "
                            + workingDirectory);
        }
        
        if(saveLastDir)
        {
            workDir = workingDirectory;
        }
        
        return workingDirectory;
    }
    
    public static DownloadThread download(URL url, File target)
            throws Exception
    {
        final DownloadThread thread = DownloadThread.newInstance(url, target);
        
        return thread;
    }
    
    public static String executePost(String targetURL, String urlParameters,
            String keyFileName)
    {
        final String protocol = targetURL.substring(4);
        HttpURLConnection connection = null;
        try
        {
            final URL url = new URL(targetURL);
            if (protocol.contains("https"))
            {
                connection = (HttpsURLConnection) url.openConnection();
            }
            else
            {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            
            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            connection.connect();
            
            if (protocol.contains("https"))
            {
                final Certificate[] certs = ((HttpsURLConnection) connection)
                        .getServerCertificates();
                
                final byte[] bytes = new byte[294];
                final DataInputStream dis = new DataInputStream(
                        getResourceAsStream("keys/" + keyFileName));
                dis.readFully(bytes);
                dis.close();
                
                final Certificate c = certs[0];
                final PublicKey pk = c.getPublicKey();
                final byte[] data = pk.getEncoded();
                
                for (int i = 0; i < data.length; i++)
                {
                    if (data[i] == bytes[i])
                    {
                        continue;
                    }
                    throw new RuntimeException("Public key mismatch");
                }
            }
            
            final DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            
            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(
                    is));
            
            final StringBuffer response = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null)
            {
                response.append(line);
            }
            rd.close();
            
            final String str1 = response.toString();
            return str1;
        }
        catch (final Exception e)
        {
            MCLogger.error(e.getLocalizedMessage());
            return null;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }
    
    public static void openLink(URI uri)
    {
        try
        {
            final Object o = Class.forName("java.awt.Desktop")
                    .getMethod("getDesktop", new Class[0])
                    .invoke(null, new Object[0]);
            o.getClass().getMethod("browse", new Class[] { URI.class })
                    .invoke(o, new Object[] { uri });
        }
        catch (final Throwable e)
        {
            System.out.println("Failed to open link " + uri.toString());
        }
    }
}
