package it.pgp.squerez;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.pgp.squerez.responses.TorrentStatus;

/**
 * Adapted from XFiles BrowserAdapter
 */

public class TorrentAdapter extends ArrayAdapter<TorrentStatus> {

    protected LayoutInflater inflater;
    protected final int containerLayout;
    public List<TorrentStatus> objects,currentObjects;
    // "objects" (full objects) as reference list, and currentObjects for quick find currently shown results

    TorrentAdapter(Context context, List<TorrentStatus> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.objects = objects;
        this.currentObjects = objects;
        inflater = LayoutInflater.from(context);
        containerLayout = R.layout.torrent_item;
    }

    public void setNewContent(List<TorrentStatus> objects) {
        this.objects.clear();
        this.objects.addAll(objects);
        this.currentObjects = this.objects; // FIXME invalidates selection mode and quick find
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        objects.clear();
        currentObjects.clear();
        super.clear();
    }

    @Override
    public long getItemId(int position) {
        return position; //return position here
    }

    @Override
    public TorrentStatus getItem(int position) {
        return currentObjects.get(position);
    }

    @Override
    public int getCount() {
        return currentObjects.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        for(int i=0; i<getCount() ; i++) {
            TorrentStatus b = getItem(i);
            if (!b.isChecked()) return false;
        }
        return true;
    }

    public int getSelectedCount() {
        int selectedCount=0;
        for(int i=0; i<getCount() ; i++) {
            TorrentStatus b = getItem(i);
            if (b.isChecked()) selectedCount++;
        }
        return selectedCount;
    }

    public List<String> getSelectedItemsAsNameOnlyStrings() {
        List<String> selectedItems = new ArrayList<>();
        for(int i=0; i<getCount() ; i++) {
            TorrentStatus b = getItem(i);
            if (b.isChecked()) selectedItems.add(b.path);
        }
        return selectedItems;
    }


    public void toggleSelectOne(TorrentStatus b) {
        b.toggle();
        notifyDataSetChanged();
    }

    public void selectAll() {
        for(int i=0; i<getCount() ; i++)
            getItem(i).setChecked(true);
        notifyDataSetChanged();
    }

    public void selectNone() {
        for(int i=0; i<getCount() ; i++)
            getItem(i).setChecked(false);
        notifyDataSetChanged();
    }

    public void invertSelection() {
        for(int i=0; i<getCount() ; i++)
            getItem(i).toggle();
        notifyDataSetChanged();
    }

    // from RAR UI
    public void filterSelection(String content, boolean selectOrDeselect) {
        for(int i=0; i<getCount() ; i++) {
            TorrentStatus b = getItem(i);
            if (b.path.contains(content))
                b.setChecked(selectOrDeselect);
        }
        notifyDataSetChanged();
    }

    public void filterObjects(CharSequence content, boolean ignoreCase) {
        if (content.equals("")) {
            // no filter, revert to full list of objects
            currentObjects = objects;
        }
        else {
            currentObjects = new ArrayList<>();
            // TODO to be replace with recursive filtering (on currentObjects)
            if (ignoreCase) {
                for (TorrentStatus b : objects)
                    if (b.path.toLowerCase().contains(content.toString().toLowerCase())) currentObjects.add(b);
            }
            else {
                for (TorrentStatus b : objects)
                    if (b.path.contains(content)) currentObjects.add(b);
            }
        }
        notifyDataSetChanged();
    }

    public static class TorrentStatusViewHolder {
        TextView path,upSpeed,downSpeed,currentSize,totalSize,completePercentage,activePeers,totalPeers,activeSeeds,totalSeeds,torrentState;
        ProgressBar progressBar;

        public TorrentStatusViewHolder(TextView path,
                                       TextView upSpeed,
                                       TextView downSpeed,
                                       TextView currentSize,
                                       TextView totalSize,
                                       TextView completePercentage,
                                       TextView activePeers,
                                       TextView totalPeers,
                                       TextView activeSeeds,
                                       TextView totalSeeds,
                                       ProgressBar progressBar,
                                       TextView torrentState) {
            this.path = path;
            this.upSpeed = upSpeed;
            this.downSpeed = downSpeed;
            this.currentSize = currentSize;
            this.totalSize = totalSize;
            this.completePercentage = completePercentage;
            this.activePeers = activePeers;
            this.totalPeers = totalPeers;
            this.activeSeeds = activeSeeds;
            this.totalSeeds = totalSeeds;
            this.progressBar = progressBar;
            this.torrentState = torrentState;
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        TorrentStatus item = this.getItem(position);
        TextView path,upSpeed,downSpeed,currentSize,totalSize,completePercentage,activePeers,totalPeers,activeSeeds,totalSeeds,torrentState;
        ProgressBar progressBar;

        if(convertView == null){
            convertView = inflater.inflate(containerLayout, null);

            path = convertView.findViewById(R.id.torrentItem_path);
            upSpeed = convertView.findViewById(R.id.torrentItem_upSpeed);
            downSpeed = convertView.findViewById(R.id.torrentItem_downSpeed);
            currentSize = convertView.findViewById(R.id.torrentItem_currentSize);
            totalSize = convertView.findViewById(R.id.torrentItem_totalSize);
            completePercentage = convertView.findViewById(R.id.torrentItem_completePercentage);
            activePeers = convertView.findViewById(R.id.torrentItem_activePeers);
            totalPeers = convertView.findViewById(R.id.torrentItem_totalPeers);
            activeSeeds = convertView.findViewById(R.id.torrentItem_activeSeeds);
            totalSeeds = convertView.findViewById(R.id.torrentItem_totalSeeds);
            progressBar = convertView.findViewById(R.id.torrentItem_currentProgress);
            torrentState = convertView.findViewById(R.id.torrentItem_status);

            convertView.setTag(new TorrentStatusViewHolder(
                    path,
                    upSpeed,
                    downSpeed,
                    currentSize,
                    totalSize,
                    completePercentage,
                    activePeers,
                    totalPeers,
                    activeSeeds,
                    totalSeeds,
                    progressBar,
                    torrentState
            ));
        }
        else {
            TorrentStatusViewHolder viewHolder = (TorrentStatusViewHolder)convertView.getTag();
            path = viewHolder.path;
            upSpeed = viewHolder.upSpeed;
            downSpeed = viewHolder.downSpeed;
            currentSize = viewHolder.currentSize;
            totalSize = viewHolder.totalSize;
            completePercentage = viewHolder.completePercentage;
            activePeers = viewHolder.activePeers;
            totalPeers = viewHolder.totalPeers;
            activeSeeds = viewHolder.activeSeeds;
            totalSeeds = viewHolder.totalSeeds;
            progressBar = viewHolder.progressBar;
            torrentState = viewHolder.torrentState;
        }

        convertView.setBackgroundColor(item.isChecked()? 0x9934B5E4: Color.TRANSPARENT);

        path.setText(item.path);
        upSpeed.setText(((int)(item.upSpeed/1000))+"");
        downSpeed.setText(((int)(item.downSpeed/1000))+"");
        currentSize.setText(item.currentProgress+" MB");
        totalSize.setText(item.totalSize+" MB");
        completePercentage.setText(item.completePercentage+" %");
        activePeers.setText(item.activePeers+"");
        totalPeers.setText(item.totalPeers+"");
        activeSeeds.setText(item.activeSeeds+"");
        totalSeeds.setText(item.totalSeeds+"");
        progressBar.setProgress((int)item.completePercentage);
        if(item.torrentState == null) {
            torrentState.setText("");
            torrentState.setBackgroundResource(R.color.transparent);
        }
        else {
            torrentState.setText(item.torrentState.name());
            torrentState.setBackgroundResource(item.torrentState.getColorRes());
        }

        return convertView;
    }

}

