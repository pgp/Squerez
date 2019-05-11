package it.pgp.squerez.utils;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.gudy.azureus2.ui.console.CommandReader;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import it.pgp.squerez.MainActivity;

public class StringQueueCommandReader extends CommandReader {
    public final LinkedBlockingQueue<String> Q = new LinkedBlockingQueue<>();
    public static final File torrentsDir = new File(Environment.getExternalStorageDirectory(),PlatformManagerImpl.TORRENTS_RELPATH);
    public static final File tmpDir = new File(Environment.getExternalStorageDirectory(),PlatformManagerImpl.TMP_RELPATH);
    public static final File downloadsDir = new File(Environment.getExternalStorageDirectory(),PlatformManagerImpl.DOWNLOADS_RELPATH);
    boolean closed = false;

    public static final StringQueueCommandReader closedReader = new StringQueueCommandReader(){{
       closed=true;
    }};

    @Override
    public void close() throws IOException {
        writeLine("quit\n");
        closed = true;
    }

    public void removeTorrentsWithoutOrigins(int... indices) {
        if(indices.length==0 || indices[0]<=0)
            writeLine("r all\n");
        else for (int idx: indices)
            writeLine("r "+idx+"\n");
        MainActivity.torrentAdapter.clear();
    }

    public void removeAllDownloadedFiles() {
        try {
            FileUtils.cleanDirectory(downloadsDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllTorrents() {
        removeTorrentsWithoutOrigins();

        // this is needed for recheck to work correctly (otherwise it could re-add already removed torrents)
        new Thread(()->{
            try {
                Thread.sleep(1000);
                FileUtils.cleanDirectory(torrentsDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void cleanTmpDir() {
        try {
            FileUtils.cleanDirectory(tmpDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readdTmpTorrentFilesForRecheck() {
        // re-add, will automatically recheck against already existing files
        MainActivity.active.set(false); // FIXME sloppy, non-thread safe for System.in access, should at least use synchronized here and in TorrentMonitor
        for(File tmpTorrentFile: tmpDir.listFiles())
            writeLine("a "+tmpTorrentFile.getAbsolutePath()+"\n");
        MainActivity.active.set(true);
        synchronized(MainActivity.active) {
            MainActivity.active.notifyAll();
        }
    }

    @Override
    public void recheckTorrents(int... indices) {
        // standard way, not working, error is: Initiating recheck failed
//        if (indices.length==0 || indices[0]<=0)
//            writeLine("c all"+"\n");
//        else for(int i: indices) writeLine("c "+i+"\n");

        torrentsDir.mkdirs();
        tmpDir.mkdirs();
        if (indices.length==0 || indices[0]<=0) {
            removeTorrentsWithoutOrigins();

            cleanTmpDir();

            int cnt = 0;
            for(File torrentFile: torrentsDir.listFiles()) {
                torrentFile.renameTo(new File(tmpDir,cnt+".torrent"));
                cnt++;
            }
            readdTmpTorrentFilesForRecheck();
        }
        else {
            // remove individual files
            final Set<String> filenames = new HashSet<>();
            for (int idx: indices) {
                filenames.add(new File(MainActivity.torrentAdapter.getItem(idx-1).origin).getName());
            }
            removeTorrentsWithoutOrigins(indices);
            cleanTmpDir();

            int cnt = 0;
            for (String filename : filenames) {
                if (!new File(torrentsDir,filename).renameTo(new File(tmpDir,cnt+".torrent")))
                    throw new RuntimeException("Unable to perform rename of temporary torrent file");
                cnt++;
            }
            readdTmpTorrentFilesForRecheck();
        }
    }

    @Override
    public String readLine() throws IOException {
        if (closed && Q.isEmpty()) throw new IOException(getClass().getName()+" closed");
        try {
            return Q.take();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean writeLine(String l) {
        if (closed) {
            Log.e(getClass().getName(),"closed");
            return false;
        }
        try {
            Q.put(l);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("Not implemented");
    }

    public StringQueueCommandReader() {
        super(null);
    }

    public StringQueueCommandReader(Object lock) {
        super(null);
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        throw new IOException("Not implemented");
    }

    @Override
    public int read() throws IOException {
        throw new IOException("Not implemented");
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        throw new IOException("Not implemented");
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IOException("Not implemented");
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        super.mark(readAheadLimit);
    }

    @Override
    public void reset() {
        Q.clear();
    }
}
