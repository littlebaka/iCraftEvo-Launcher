package com.kokakiwi.mclauncher.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.MCLogger;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.java.DownloadThread;
import com.kokakiwi.mclauncher.utils.java.SystemUtils;
import com.kokakiwi.mclauncher.utils.java.Utils;
import com.kokakiwi.mclauncher.utils.java.Version;

public class GameUpdater implements Runnable
{
    private final LauncherFrame launcherFrame;
    private final Launcher      launcher;
    
    private boolean             lzmaSupported;
    private boolean             pack200Supported;
    public boolean              fatalError            = false;
    public String               fatalErrorDescription;
    public boolean              shouldUpdate          = false;
    public String               latestVersionToUpdate = null;
    
    private URL[]               jarUrls;
    private URL[]               additionalsUrls;
    private URL                 nativeUrl;
    
    public GameUpdater(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
        launcher = launcherFrame.launcher;
    }
    
    public void run()
    {
        init();
        setPercentage(5);
        
        try
        {
            loadJarUrls();
            loadAdditionalsUrls();
            loadNativeUrl();
            
            final String path = Utils.getWorkingDirectory(launcherFrame)
                    + File.separator + "bin" + File.separatorChar;
            final File dir = new File(path);
            
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            
            // Check version
            boolean forceUpdate = launcherFrame.getConfig().getString(
                    "force-update") == null ? false : true;
            
            final String latestVersion = launcherFrame.getConfig().getString(
                    "latestVersion");
            final File versionFile = new File(dir, "version");
            if (latestVersion != null)
            {
                if (versionFile.exists())
                {
                    if ((!latestVersion.equals(readVersionFile(versionFile))
                            || latestVersion.equals("-1")) && !launcherFrame.getConfig().getBoolean("launcher.offlineMode"))
                    {
                        MCLogger.info("Update available for Base game. New version : "
                                + latestVersion);
                        if (launcherFrame.getConfig().getBoolean(
                                "updater.updateIfNewVersionAvailable"))
                        {
                            shouldUpdate = true;
                        }
                    }
                }
                else
                {
                    forceUpdate = true;
                }
            }
            
            final boolean useCustomVersion = launcherFrame.getConfig()
                    .getBoolean("updater.customVersion.use");
            Version latestCustomVersion = new Version();
            if (useCustomVersion)
            {
                latestCustomVersion = Version.parseString(Utils.executePost(
                        launcherFrame.getConfig().getString(
                                "updater.customVersion.checkUrl"), "", ""));
                final File customVersionFile = new File(dir, launcherFrame
                        .getConfig()
                        .getString("updater.customVersion.fileName"));
                if (customVersionFile.exists())
                {
                    final Version currentVersion = Version
                            .parseString(readVersionFile(customVersionFile));
                    if (latestCustomVersion.compareTo(currentVersion) > 0)
                    {
                        MCLogger.info("Update available for custom. New version : "
                                + latestCustomVersion);
                        
                        if (launcherFrame.getConfig().getBoolean(
                                "updater.customVersion.updateIfAvailable"))
                        {
                            shouldUpdate = true;
                            latestVersionToUpdate = latestCustomVersion
                                    .toString();
                        }
                    }
                }
                else
                {
                    shouldUpdate = true;
                }
            }
            
            // Ask update if available
            
            if (shouldUpdate
                    && !forceUpdate
                    && launcherFrame.getConfig().getBoolean(
                            "updater.askUpdateIfAvailable"))
            {
                checkShouldUpdate();
            }
            
            // Do update
            if (shouldUpdate || forceUpdate)
            {
                writeVersionFile(versionFile, latestVersion.equals("-1") ? ""
                        : latestVersion);
                if (useCustomVersion)
                {
                    final File customVersionFile = new File(dir, launcherFrame
                            .getConfig().getString(
                                    "updater.customVersion.fileName"));
                    writeVersionFile(customVersionFile,
                            latestCustomVersion.toString());
                }
                
                downloadFiles(path);
                extractJars(path);
                extractAdditionals(Utils.getWorkingDirectory(launcherFrame)
                        .getAbsolutePath() + File.separatorChar);
                extractNative(path);
            }
        }
        catch (final Exception e)
        {
            MCLogger.error(e.getLocalizedMessage());
        }
        
        setPercentage(90);
    }
    
    private void loadJarUrls() throws Exception
    {
        launcher.setState(State.DETERMINING_PACKAGE);
        final List<String> jarList = launcherFrame.getConfig().getStringList(
                "updater.jarList");
        jarUrls = new URL[jarList.size()];
        
        for (int i = 0; i < jarList.size(); i++)
        {
            jarUrls[i] = new URL(jarList.get(i));
        }
    }
    
    private void loadAdditionalsUrls() throws Exception
    {
        final List<String> addList = launcherFrame.getConfig().getStringList(
                "updater.additionalsFiles");
        additionalsUrls = new URL[addList.size()];
        
        for (int i = 0; i < addList.size(); i++)
        {
            if (addList.get(i) != null)
            {
                additionalsUrls[i] = new URL(addList.get(i));
            }
        }
    }
    
    private void loadNativeUrl() throws Exception
    {
        final SystemUtils.OS osName = SystemUtils.getSystemOS();
        String nativeJar = null;
        
        if (osName == SystemUtils.OS.unknown)
        {
            fatalErrorOccured("OS (" + System.getProperty("os.name")
                    + ") not supported");
        }
        else
        {
            nativeJar = launcherFrame.getConfig().getString(
                    "updater.nativesList." + osName.name());
        }
        
        if (nativeJar == null)
        {
            fatalErrorOccured("no lwjgl natives files found");
        }
        else
        {
            nativeJar = trimExtensionByCapabilities(nativeJar);
            nativeUrl = new URL(nativeJar);
        }
    }
    
    private void downloadFiles(String path) throws Exception
    {
        final File versionFile = new File(path, "md5s");
        final Properties md5s = new Properties();
        if (versionFile.exists())
        {
            try
            {
                final FileInputStream fis = new FileInputStream(versionFile);
                md5s.load(fis);
                fis.close();
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
        launcher.setState(State.DOWNLOADING);
        
        final int downloadNum = jarUrls.length + additionalsUrls.length + 1;
        final URL[] urls = new URL[downloadNum];
        int pointer;
        for (pointer = 0; pointer < jarUrls.length; pointer++)
        {
            urls[pointer] = jarUrls[pointer];
        }
        for (int i = 0; i < additionalsUrls.length; i++)
        {
            urls[i + pointer] = additionalsUrls[i];
        }
        urls[downloadNum - 1] = nativeUrl;
        
        final DownloadThread[] threads = new DownloadThread[downloadNum];
        
        int totalFilesSize = 0;
        int totalDownloadedSize = 0;
        for (int i = 0; i < downloadNum; i++)
        {
            if (urls[i] != null)
            {
                final String fileName = getFileName(urls[i]);
                final File target = new File(path, fileName);
                final DownloadThread thread = new DownloadThread(urls[i],
                        target);
                thread.initConnection();
                totalFilesSize += thread.getFileSize();
                
                threads[i] = thread;
            }
        }
        
        for (final DownloadThread thread : threads)
        {
            if (thread != null)
            {
                MCLogger.info("Download " + thread.getFileName());
                thread.start();
                while (!thread.isDownloaded())
                {
                    if (thread.getFileSize() > 0)
                    {
                        String subTaskMessage = launcherFrame.locale
                                .getString("updater.retrieving")
                                + ": "
                                + thread.getFileName()
                                + " "
                                + thread.getDownloadedSize()
                                * 100
                                / thread.getFileSize() + "%";
                        if (thread.getDownloadSpeed() > 0)
                        {
                            subTaskMessage += " @ " + thread.getDownloadSpeed()
                                    + " Kb/sec";
                        }
                        launcher.subtaskMessage = subTaskMessage;
                        
                        final int percent = 10
                                + (totalDownloadedSize + thread
                                        .getDownloadedSize()) * 45
                                / totalFilesSize;
                        setPercentage(percent);
                    }
                }
                
                totalDownloadedSize += thread.getFileSize();
            }
        }
        
        launcher.subtaskMessage = "";
    }
    
    private void extractJars(String path) throws Exception
    {
        launcher.setState(State.EXTRACTING_PACKAGES);
        
        for (final URL url : jarUrls)
        {
            final String fileName = getFileName(url);
            final File file = new File(path + fileName);
            extract(file, path, "jar");
        }
    }
    
    private void extractAdditionals(String path) throws Exception
    {
        for (final URL url : additionalsUrls)
        {
            if (url != null)
            {
                final String fileName = getFileName(url);
                final File file = new File(
                        Utils.getWorkingDirectory(launcherFrame)
                                + File.separator + "bin" + File.separatorChar
                                + fileName);
                extract(file, path);
            }
        }
    }
    
    private void extractNative(String path) throws Exception
    {
        final String fileName = getFileName(nativeUrl);
        final File file = new File(path + fileName);
        extract(file, path + "natives" + File.separatorChar);
    }
    
    private void extract(File file, String path) throws Exception
    {
        extract(file, path, "");
    }
    
    private void extract(File file, String path, String excludes)
            throws Exception
    {
        final File dir = new File(path);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        
        File tmpFile = file;
        
        final String fileName = getFileName(file.toURI().toURL());
        final String extPart = fileName.substring(fileName.indexOf('.') + 1);
        
        String[] exts = new String[0];
        if (extPart.contains("."))
        {
            exts = fileName.substring(fileName.indexOf('.') - 1).split(".");
        }
        else
        {
            exts = new String[1];
            exts[0] = extPart;
        }
        
        for (int i = exts.length - 1; i >= 0; i--)
        {
            final String ext = exts[i];
            if (ext.equalsIgnoreCase("pack") && !excludes.contains("pack"))
            {
                extractPack(tmpFile.getAbsolutePath(), tmpFile
                        .getAbsolutePath().replace(".pack", ""));
                tmpFile = new File(tmpFile.getAbsolutePath().replace(".pack",
                        ""));
            }
            else if (ext.equalsIgnoreCase("lzma") && !excludes.contains("lzma"))
            {
                extractLZMA(tmpFile.getAbsolutePath(), tmpFile
                        .getAbsolutePath().replace(".pack", ""));
                tmpFile = new File(tmpFile.getAbsolutePath().replace(".lzma",
                        ""));
            }
            else if (ext.equalsIgnoreCase("jar") && !excludes.contains("jar"))
            {
                extractJar(tmpFile, path);
                i = -1;
            }
            else if (ext.equalsIgnoreCase("zip") && !excludes.contains("zip"))
            {
                extractZip(tmpFile, path);
                i = -1;
            }
            
        }
    }
    
    public URL[] getJarURLs()
    {
        final int downloadNum = jarUrls.length;
        final URL[] urls = new URL[downloadNum];
        int pointer;
        for (pointer = 0; pointer < jarUrls.length; pointer++)
        {
            urls[pointer] = jarUrls[pointer];
        }
        
        return urls;
    }
    
    private void checkShouldUpdate()
    {
        launcher.pauseAskUpdate = true;
        while (launcher.pauseAskUpdate)
        {
            try
            {
                Thread.sleep(1000L);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private void setPercentage(int percent)
    {
        launcher.setPercentage(percent);
    }
    
    private void fatalErrorOccured(String error)
    {
        fatalError = true;
        fatalErrorDescription = "Fatal error occured (" + launcher.getState()
                + "): " + error;
    }
    
    private String trimExtensionByCapabilities(String file)
    {
        if (!pack200Supported)
        {
            file = file.replaceAll(".pack", "");
        }
        
        if (!lzmaSupported)
        {
            file = file.replaceAll(".lzma", "");
        }
        return file;
    }
    
    private String readVersionFile(File file) throws Exception
    {
        final DataInputStream dis = new DataInputStream(new FileInputStream(
                file));
        final String version = dis.readUTF();
        dis.close();
        return version;
    }
    
    private void writeVersionFile(File file, String version) throws Exception
    {
        if (!file.exists())
        {
            file.createNewFile();
        }
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(
                file));
        dos.writeUTF(version);
        dos.close();
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
    
    private void extractLZMA(String in, String out) throws Exception
    {
        final File f = new File(in);
        if (!f.exists())
        {
            return;
        }
        final FileInputStream fileInputHandle = new FileInputStream(f);
        
        final Class<?> clazz = Class.forName("LZMA.LzmaInputStream");
        final Constructor<?> constructor = clazz
                .getDeclaredConstructor(new Class[] { InputStream.class });
        
        InputStream inputHandle = (InputStream) constructor
                .newInstance(new Object[] { fileInputHandle });
        
        OutputStream outputHandle = new FileOutputStream(out);
        
        final byte[] buffer = new byte[16384];
        
        int ret = inputHandle.read(buffer);
        while (ret >= 1)
        {
            outputHandle.write(buffer, 0, ret);
            ret = inputHandle.read(buffer);
        }
        
        inputHandle.close();
        outputHandle.close();
        
        outputHandle = null;
        inputHandle = null;
        
        f.delete();
    }
    
    private void extractPack(String in, String out) throws Exception
    {
        final File f = new File(in);
        if (!f.exists())
        {
            return;
        }
        
        final FileOutputStream fostream = new FileOutputStream(out);
        final JarOutputStream jostream = new JarOutputStream(fostream);
        
        final Pack200.Unpacker unpacker = Pack200.newUnpacker();
        unpacker.unpack(f, jostream);
        jostream.close();
        
        f.delete();
    }
    
    @SuppressWarnings("unchecked")
    private void extractZip(File file, String path) throws Exception
    {
        final int initialPercentage = launcher.getPercentage();
        
        final ZipFile zipFile = new ZipFile(file);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile
                .entries();
        
        int totalSizeExtract = 0;
        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                continue;
            }
            
            totalSizeExtract += entry.getSize();
        }
        int currentSizeExtract = 0;
        entries = (Enumeration<ZipEntry>) zipFile.entries();
        
        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                File dir = new File(path + entry.getName());
                if(!dir.exists())
                {
                    dir.mkdirs();
                }
                continue;
            }
            
            final File f = new File(path + entry.getName());
            if (f.exists() && !f.delete())
            {
                continue;
            }
            
            final InputStream in = zipFile.getInputStream(zipFile
                    .getEntry(entry.getName()));
            final OutputStream out = new FileOutputStream(new File(path
                    + entry.getName()));
            
            final byte[] buffer = new byte[65536];
            int bufferSize;
            while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, bufferSize);
                currentSizeExtract += bufferSize;
                
                launcher.setPercentage(initialPercentage + currentSizeExtract
                        * 20 / totalSizeExtract);
                launcher.subtaskMessage = launcherFrame.locale
                        .getString("updater.extracting")
                        + ": "
                        + entry.getName()
                        + " "
                        + currentSizeExtract
                        * 100
                        / totalSizeExtract + "%";
            }
            
            in.close();
            out.close();
        }
        
        launcher.subtaskMessage = "";
        zipFile.close();
    }
    
    private void extractJar(File file, String path) throws Exception
    {
        final int initialPercentage = launcher.getPercentage();
        final JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        
        int totalSizeExtract = 0;
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || entry.getName().indexOf('/') != -1)
            {
                continue;
            }
            
            totalSizeExtract += entry.getSize();
        }
        int currentSizeExtract = 0;
        entries = jarFile.entries();
        
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || entry.getName().indexOf('/') != -1)
            {
                continue;
            }
            
            final File f = new File(path + entry.getName());
            if (f.exists() && !f.delete())
            {
                continue;
            }
            
            final InputStream in = jarFile.getInputStream(jarFile
                    .getEntry(entry.getName()));
            final OutputStream out = new FileOutputStream(new File(path
                    + entry.getName()));
            
            final byte[] buffer = new byte[65536];
            int bufferSize;
            while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, bufferSize);
                currentSizeExtract += bufferSize;
                
                launcher.setPercentage(initialPercentage + currentSizeExtract
                        * 20 / totalSizeExtract);
                launcher.subtaskMessage = launcherFrame.locale
                        .getString("updater.extracting")
                        + ": "
                        + entry.getName()
                        + " "
                        + currentSizeExtract
                        * 100
                        / totalSizeExtract + "%";
            }
            
            in.close();
            out.close();
        }
        
        launcher.subtaskMessage = "";
        jarFile.close();
    }
    
    public String getJarName(URL url)
    {
        String fileName = getFileName(url);
        if (fileName.contains("?"))
        {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        if (fileName.endsWith(".pack.lzma"))
        {
            fileName = fileName.replaceAll(".pack.lzma", "");
        }
        else if (fileName.endsWith(".pack"))
        {
            fileName = fileName.replaceAll(".pack", "");
        }
        else if (fileName.endsWith(".lzma"))
        {
            fileName = fileName.replaceAll(".lzma", "");
        }
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    
    private void init()
    {
        try
        {
            Class.forName("LZMA.LzmaInputStream");
            lzmaSupported = true;
        }
        catch (final Throwable localThrowable)
        {
        }
        try
        {
            Pack200.class.getSimpleName();
            pack200Supported = true;
        }
        catch (final Throwable localThrowable1)
        {
        }
    }
}
