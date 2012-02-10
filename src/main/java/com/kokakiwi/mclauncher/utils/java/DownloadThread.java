package com.kokakiwi.mclauncher.utils.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

import com.kokakiwi.mclauncher.utils.MCLogger;

public class DownloadThread extends Thread
{
    public static int     MAX_UNSUCCESSFUL_ATTEMPTS = 3;
    
    private final URL     url;
    private final File    target;
    private URLConnection connection                = null;
    private boolean       downloaded                = false;
    
    private int           downloadedAmount          = 0;
    private int           fileSize                  = 0;
    private int           downloadedSize            = 0;
    private float         downloadSpeed             = 0;
    private String        etag;
    private final String  fileName;
    
    private long          downloadStartTime;
    
    public DownloadThread(URL url, File target)
    {
        this.url = url;
        this.target = target;
        
        fileName = getFileName(url);
    }
    
    public void initConnection()
    {
        try
        {
            if (connection == null)
            {
                connection = url.openConnection();
                connection.setDefaultUseCaches(false);
                if (connection instanceof HttpURLConnection)
                {
                    fileSize = connection.getContentLength();
                }
            }
        }
        catch (final IOException e)
        {
            MCLogger.error(e.getLocalizedMessage());
        }
    }
    
    public URLConnection getConnection()
    {
        return connection;
    }
    
    public boolean isDownloaded()
    {
        return downloaded;
    }
    
    public int getFileSize()
    {
        return fileSize;
    }
    
    public int getDownloadedSize()
    {
        return downloadedSize;
    }
    
    public float getDownloadSpeed()
    {
        return downloadSpeed;
    }
    
    public String getEtag()
    {
        return etag;
    }
    
    public long getDownloadStartTime()
    {
        return downloadStartTime;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    @SuppressWarnings("unused")
    @Override
    public void run()
    {
        initConnection();
        
        try
        {
            boolean downloadFile = true;
            final MessageDigest m = MessageDigest.getInstance("MD5");
            downloadStartTime = System.currentTimeMillis();
            while (downloadFile)
            {
                downloadFile = false;
                
                final InputStream in = connection.getInputStream();
                final FileOutputStream out = new FileOutputStream(target);
                int bufferSize;
                final byte[] buffer = new byte[65536];
                while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
                {
                    out.write(buffer, 0, bufferSize);
                    downloadedSize += bufferSize;
                    downloadedAmount += bufferSize;
                    
                    final long timeLapse = System.currentTimeMillis()
                            - downloadStartTime;
                    
                    if (timeLapse >= 1000L)
                    {
                        downloadSpeed = (int) (downloadedAmount
                                / (float) timeLapse * 100.0f) / 100.0F;
                        
                        downloadedAmount = 0;
                        downloadStartTime += timeLapse;
                    }
                }
                in.close();
                out.close();
            }
            
            downloaded = true;
        }
        catch (final Exception e)
        {
            MCLogger.error(e.getLocalizedMessage());
        }
    }
    
    private String getFileName(URL url)
    {
        String fileName = url.getFile();
        if (fileName.contains("?"))
        {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    
    public static DownloadThread newInstance(URL url, File target)
    {
        return new DownloadThread(url, target);
    }
}