package it.pgp.squerez.enums;

import org.gudy.azureus2.core3.download.DownloadManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.pgp.squerez.R;

public enum TorrentState {
    // INITIALIZING, // e.g. downloading metafile (have to keep separate map with items, Vuze CLI doesn't add to download list till metafile downloaded)
    DOWNLOADING(1,R.color.colorPrimary),
    RECHECKING(2,R.color.colorAccent),
    COMPLETED(3,R.color.green),
    STOPPED(4,R.color.brown);

    // TODO map to DownloadManager methods or directly use them

    final int vuzeState;

    final int colorRes;

    public static Map<Integer,TorrentState> vuzeStatusesToSquerezStatues = Collections.unmodifiableMap(
            new HashMap<Integer,TorrentState>(){{
                put(DownloadManager.STATE_DOWNLOADING,DOWNLOADING);
                put(DownloadManager.STATE_CHECKING,RECHECKING);
                put(DownloadManager.STATE_SEEDING,COMPLETED);
                put(DownloadManager.STATE_STOPPED,STOPPED);
            }}
    );

    public static TorrentState fromVuzeState(int vuzeState) {
        return vuzeStatusesToSquerezStatues.get(vuzeState);
    }

    TorrentState(int vuzeState, int colorRes) {
        this.vuzeState = vuzeState;
        this.colorRes = colorRes;
    }

    public int getColorRes() {
        return colorRes;
    }
}