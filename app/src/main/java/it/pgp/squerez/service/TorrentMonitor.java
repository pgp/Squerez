package it.pgp.squerez.service;

import android.util.Log;

import org.gudy.azureus2.ui.common.Main;
import org.gudy.azureus2.ui.console.CommandReader;
import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import it.pgp.squerez.MainActivity;

public class TorrentMonitor extends Thread {

    public static TorrentMonitor instance;

    public static void enable(CommandReader br) {
        if(instance==null) {
            instance = new TorrentMonitor(br);
            instance.start();
        }
    }

    public static void disable() {
        instance = null;
    }

    // TODO to be moved in separate class
    public static void startVuzeCLIEngine() {
        new Thread(()->{
            try {
//                    Main.main(new String[]{"--ui=console"});
                Main.main(new String[0]);
                Thread.sleep(1000); // FIXME replace with synchronized wait
                enable(ConsoleInputHelperFactory.currentCommandReader);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private final CommandReader br;

    private TorrentMonitor(CommandReader br) {
        this.br = br;
    }

    @Override
    public void run() {
        try {
            for(;;) {
                Thread.sleep(2000);
                if(!MainActivity.active.get()) { // if activity is paused...
                    synchronized (MainActivity.active) {
                        MainActivity.active.wait(); // wait for it to be resumed
                    }
                }
                Log.e(getClass().getName(),"sending show torrents command");
                if(!br.writeLine("sh t\n")) break;
            }
        }
        catch(InterruptedException ignored) {}
        Log.e(getClass().getName(), "Thread ended");
    }
}
