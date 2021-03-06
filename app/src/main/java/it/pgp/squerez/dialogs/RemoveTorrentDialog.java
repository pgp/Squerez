package it.pgp.squerez.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import java.util.Collections;

import it.pgp.squerez.MainActivity;
import it.pgp.squerez.R;
import it.pgp.squerez.utils.StringQueueCommandReader;
import it.pgp.squerez.utils.XFilesUtilsLite;

public class RemoveTorrentDialog extends Dialog {

    // torrentIndex starts from 1
    public RemoveTorrentDialog(Context context, final int torrentIndex, final String originPath, final String downloadedFilesPath) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.remove_dialog);
        
        final CheckBox removeOrigin = findViewById(R.id.removeOriginCheckbox);
        final CheckBox removeDownloadedFiles = findViewById(R.id.removeDownloadedFilesCheckbox);
        
        findViewById(R.id.removeTorrentNoButton).setOnClickListener(v->dismiss());
        findViewById(R.id.removeTorrentYesButton).setOnClickListener(v->{
            ConsoleInputHelperFactory.currentCommandReader.writeLine("r "+torrentIndex+"\n");
            MainActivity.torrentAdapter.clear();
            if(removeOrigin.isChecked())
                XFilesUtilsLite.deleteFilesOrDirectories(Collections.singletonList(originPath));
            if(removeDownloadedFiles.isChecked())
                XFilesUtilsLite.deleteFilesOrDirectories(Collections.singletonList(downloadedFilesPath));
            Toast.makeText(context, "Torrent removed", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
    
    // remove all torrents
    public RemoveTorrentDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.remove_dialog);
        ((TextView)findViewById(R.id.removeTorrentCaption)).setText(context.getString(R.string.removeAllTorrentsCaption));
        
        final CheckBox removeOrigin = findViewById(R.id.removeOriginCheckbox);
        final CheckBox removeDownloadedFiles = findViewById(R.id.removeDownloadedFilesCheckbox);
        
        findViewById(R.id.removeTorrentNoButton).setOnClickListener(v->dismiss());
        findViewById(R.id.removeTorrentYesButton).setOnClickListener(v->{
            if(removeOrigin.isChecked())
                ConsoleInputHelperFactory.currentCommandReader.removeAllTorrents();
            else
                ((StringQueueCommandReader)ConsoleInputHelperFactory.currentCommandReader).removeTorrentsWithoutOrigins();
            if(removeDownloadedFiles.isChecked())
                ((StringQueueCommandReader)ConsoleInputHelperFactory.currentCommandReader).removeAllDownloadedFiles();
            Toast.makeText(context, "Torrents removed", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
    
    
}
