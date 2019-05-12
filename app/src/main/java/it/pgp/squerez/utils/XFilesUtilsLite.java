package it.pgp.squerez.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Lite version of XFilesUtilsUsingPathContent from https://github.com/pgp/XFiles
 */

public class XFilesUtilsLite {

    public int getTotalFilesCount(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f : files) {
            if (f.isDirectory()) count += getTotalFilesCount(f);
            else count++;
        }
        return count;
    }

    public void copyFileOrDirectory(File srcFileOrDir, File dstFolder) throws IOException {
        if (srcFileOrDir.isDirectory()) {
            File[] files = srcFileOrDir.listFiles();
            for (File file : files) {
                File src1 = new File(srcFileOrDir, file.getName());
                File dst1 = new File(dstFolder,srcFileOrDir.getName());
                copyFileOrDirectory(src1, dst1);
            }
        } else {
            copyFile(srcFileOrDir, new File(dstFolder,srcFileOrDir.getName()));
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();
        if (!destFile.exists()) destFile.createNewFile();

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    // to be merged in Fileopshelper interface
    public int copyFileOrEmptyDir(String sourceFile_, String destFile_) {
        File sourceFile = new File(sourceFile_);
        File destFile = new File(destFile_);

        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs())
                return -1; // mkdirs error
        }

        if (!destFile.exists()) {
            if (sourceFile.isFile()) {
                try {
                    if (!destFile.createNewFile())
                        return -2; // mkfile error
                }
                catch (IOException i) {
                    return -3; // mkfile error
                }
            }
            else {
                if (!destFile.mkdirs())
                    return -4; // mkdirs error
            }
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
            return 0;
        }
        catch (IOException i) {
            return -5; // copy error
        }
    }

    // copies regular files and empty directories, to be used with DirTreeWalker classes
    public void copyFileOrEmptyDir(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            if (sourceFile.isFile()) destFile.createNewFile();
            else {
                destFile.mkdirs();
                return;
            }
        }

        try (FileChannel source  = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    public void copyFilesToDirectory(List files, File dstFolder) throws IOException {
        for (Object pathname: files) {
            if(pathname instanceof String)
                copyFileOrDirectory(new File((String)pathname),dstFolder);
            else if (pathname instanceof File)
                copyFileOrDirectory((File)pathname,dstFolder);
            else throw new IOException("Invalid type in file list: "+pathname.getClass().getName());
        }
    }

    public void moveFilesToDirectory(List files, File dstFolder) throws IOException {
        for (Object pathname : files) {
            File file = (pathname instanceof String)?new File((String) pathname): (File) pathname;
            // removed Commons IO, allow only rename-based move
            File destFile = new File(dstFolder,file.getName());
            if (!file.renameTo(destFile)) throw new IOException("Cannot rename filesystem node");
        }
    }

    public static void deleteDirectory(File dir) {
        cleanDirectory(dir);
        dir.delete();
    }

    public static void cleanDirectory(File dir) {
        File[] files = dir.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    public static void deleteFilesOrDirectories(List pathnames) {
        for (Object pathname : pathnames) {
            File file = (pathname instanceof String)? new File((String) pathname): (File) pathname;
            if (file.isDirectory()) deleteDirectory(file);
            else file.delete();
        }
    }

    public boolean renameFile(Object oldPathname, Object newPathname) {
        File f = (oldPathname instanceof String)?new File((String) oldPathname): (File) oldPathname;
        File g = (newPathname instanceof String)?new File((String) newPathname): (File) newPathname;
        return f.renameTo(g);
    }

}

