package it.pgp.squerez;

import java.net.URLDecoder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import it.pgp.squerez.dialogs.AboutDialog;
import it.pgp.squerez.dialogs.RemoveTorrentDialog;
import it.pgp.squerez.dialogs.ThrottleTorrentDialog;
import it.pgp.squerez.enums.Permissions;
import it.pgp.squerez.responses.TorrentStatus;
import it.pgp.squerez.service.BaseBackgroundService;
import it.pgp.squerez.service.TorrentService;
import it.pgp.squerez.utils.Misc;
import it.pgp.squerez.utils.StringQueueCommandReader;

public class MainActivity extends Activity {

    @Override
    protected void onPause() {
        super.onPause();
        active.set(false);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addTorrentLayout.setVisibility(lastVisibilityStatusOfAddTorrentLayout); // restore survived status across onDestroy calls, if any
        active.set(true);
        synchronized(active) {
            active.notifyAll();
        }
    }

    public static final AtomicBoolean active = new AtomicBoolean(false);

    LinearLayout addTorrentLayout;
    EditText magnetLink;
    ImageButton startDownload;
    ListView torrentListView;
    public static TorrentAdapter torrentAdapter;
    public static MainActivity mainActivity;

    public static int lastVisibilityStatusOfAddTorrentLayout = View.VISIBLE;

    public boolean checkDangerousPermissions() {
        EnumSet<Permissions> nonGrantedPerms = EnumSet.noneOf(Permissions.class);
        for (Permissions p : Permissions.values()) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),p.value()) != PackageManager.PERMISSION_GRANTED) {
                nonGrantedPerms.add(p);
            }
        }

        return nonGrantedPerms.isEmpty();
    }

    void startPermissionManagementActivity() {
        Intent i = new Intent(this,PermissionManagementActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public void removeOrRecheckAll(View button) {
        switch(button.getId()) {
            case R.id.recheckAllTorrentsButton:
                ConsoleInputHelperFactory.currentCommandReader.recheckTorrents();
                Toast.makeText(this, "Rechecking started", Toast.LENGTH_SHORT).show();
                break;
            case R.id.removeAllTorrentsButton:
                new RemoveTorrentDialog(this).show();
                break;
            default:
                Toast.makeText(this, "Invalid button ID", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateFromSelfIntent(Intent intent) {
        showAddTorrentLayout();
        boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        if(launchedFromHistory) return; // avoid spurious download intents when re-opening from Recent Apps menu
        try {
            Uri data = intent.getData();
            if (data == null) return;
            // start/resume Squerez with pre-populated edittext
            String magnetOrTorrentPath = data.toString();
            if (data.getScheme()!=null)
                switch(data.getScheme()) {
                    case "magnet":
                        magnetLink.setText(magnetOrTorrentPath);
                        break;
                    case "file":
                        magnetLink.setText(Misc.simpleEscapeDoubleQuotes(URLDecoder.decode(magnetOrTorrentPath,"UTF-8").substring(7)));
                        break;
                    case "content":
                        String tmpfilepath = Misc.dumpContentUriIntoTmpFile(this,data,((StringQueueCommandReader)ConsoleInputHelperFactory.currentCommandReader).tmpDir);
                        magnetLink.setText(tmpfilepath);
                        break;
                }
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to convert URI to magnet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.torrent_item, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        switch (item.getItemId()) {
            case R.id.itemRecheck:
                ConsoleInputHelperFactory.currentCommandReader.recheckTorrents(pos+1);
                break;
            case R.id.itemRemove:
                TorrentStatus ts = torrentAdapter.getItem(pos);
                new RemoveTorrentDialog(this,ts.index,ts.origin,ts.path).show();
                break;
            case R.id.itemThrottleSpeeds:
                new ThrottleTorrentDialog(this, torrentAdapter.getItem(pos)).show();
                break;
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        updateFromSelfIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mainActivity = this;

        if(!checkDangerousPermissions()) {
            startPermissionManagementActivity();
            return;
        }

        setContentView(R.layout.activity_main);
        addTorrentLayout = findViewById(R.id.addTorrentLayout);
        magnetLink = findViewById(R.id.magnetLinkEditText);
        startDownload = findViewById(R.id.startDownloadButton);
        startDownload.setOnClickListener(v->{
            String ml = magnetLink.getText().toString();
            if(ml.isEmpty()) Toast.makeText(MainActivity.this, "Magnet link field is empty", Toast.LENGTH_SHORT).show();
            else {
                ConsoleInputHelperFactory.currentCommandReader.writeLine("a "+ml+"\n");
                Toast.makeText(MainActivity.this, "Magnet link added", Toast.LENGTH_SHORT).show();
                magnetLink.setText("");
                toggleAddTorrentLayout(null);
            }
        });
        torrentAdapter = new TorrentAdapter(this,new ArrayList<>());
        torrentListView = findViewById(R.id.torrentListView);
        torrentListView.setAdapter(torrentAdapter);
        registerForContextMenu(torrentListView);

        updateFromSelfIntent(getIntent());
        startTorrentService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivity = null;
    }

    public void startTorrentService() {
        Intent startIntent = new Intent(MainActivity.this, TorrentService.class);
        startIntent.setAction(BaseBackgroundService.START_ACTION);
        startService(startIntent);
    }

    public void shutdownTorrentService(View unused) {
        Intent stopIntent = new Intent(this, TorrentService.class);
        stopIntent.setAction(BaseBackgroundService.CANCEL_ACTION);
        startService(stopIntent);
    }

    public void about(View unused) {
        new AboutDialog(this).show();
    }

    public void toggleAddTorrentLayout(View unused) {
        lastVisibilityStatusOfAddTorrentLayout = addTorrentLayout.getVisibility()==View.GONE?View.VISIBLE:View.GONE;
        addTorrentLayout.setVisibility(lastVisibilityStatusOfAddTorrentLayout);
    }

    public void showAddTorrentLayout() {
        lastVisibilityStatusOfAddTorrentLayout = View.VISIBLE;
        addTorrentLayout.setVisibility(lastVisibilityStatusOfAddTorrentLayout);
    }


}