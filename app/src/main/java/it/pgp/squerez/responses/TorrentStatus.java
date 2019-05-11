package it.pgp.squerez.responses;

import android.widget.Checkable;

import java.io.Serializable;

import it.pgp.squerez.enums.TorrentState;

public class TorrentStatus implements Serializable, Checkable, Comparable<TorrentStatus> {

    public int index;
    public String origin; // torrent file or magnet link
    public String path; // download path, file or directory, can be relative to downloads dir

    // EITHER
    public long currentProgress; // bytes
    public long totalSize; // bytes
    // OR
    public float completePercentage;

    // transfer speeds, kbps
    public float downSpeed;
    public float upSpeed;

    public int totalSeeds;
    public int activeSeeds;
    public int totalPeers;
    public int activePeers;


    private boolean checked = false;

    public TorrentState torrentState; // if true, only seeding, torrent can be safely removed

    public TorrentStatus(int index,
                         String origin,
                         String path,
                         long currentProgress,
                         long totalSize,
                         float completePercentage,
                         float downSpeed,
                         float upSpeed,
                         int totalSeeds,
                         int activeSeeds,
                         int totalPeers,
                         int activePeers,
                         TorrentState torrentState) {
        this.index = index; // changes only upon other items removal
        this.origin = origin; // FIXED
        this.path = path; // FIXED
        this.currentProgress = currentProgress;
        this.totalSize = totalSize; // FIXED
        this.completePercentage = completePercentage;
        this.downSpeed = downSpeed;
        this.upSpeed = upSpeed;
        this.totalSeeds = totalSeeds;
        this.activeSeeds = activeSeeds;
        this.totalPeers = totalPeers;
        this.activePeers = activePeers;
        this.torrentState = torrentState;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public void toggle() {
        checked = !checked;
    }

    @Override
    public int compareTo(TorrentStatus o) {
        return path.compareTo(o.path); // sort by path attribute
    }
}
