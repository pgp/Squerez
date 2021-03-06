package it.pgp.squerez;

import java.io.File;
import java.net.URLDecoder;
import java.util.concurrent.locks.LockSupport;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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
import it.pgp.squerez.dialogs.MoveTorrentDialog;
import it.pgp.squerez.dialogs.RemoveTorrentDialog;
import it.pgp.squerez.dialogs.ThrottleTorrentDialog;
import it.pgp.squerez.enums.Permissions;
import it.pgp.squerez.responses.TorrentStatus;
import it.pgp.squerez.service.BaseBackgroundService;
import it.pgp.squerez.service.TorrentService;
import it.pgp.squerez.utils.Misc;
import it.pgp.squerez.utils.StringQueueCommandReader;

public class MainActivity extends Activity {

    static {
        // avoid messing up with content URIs
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
    }

    @Override
    protected void onPause() {
        super.onPause();
        active.set(false);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeStd.run();
    }

    public static final AtomicBoolean active = new AtomicBoolean(false);

    LinearLayout addTorrentLayout;
    EditText magnetLink;
    ImageButton startDownload;
    ListView torrentListView;

    Runnable resumeStd = () -> {
        addTorrentLayout.setVisibility(lastVisibilityStatusOfAddTorrentLayout); // restore survived status across onDestroy calls, if any
        active.set(true);
        synchronized(active) {
            active.notifyAll();
        }
    };

    public static TorrentAdapter torrentAdapter;
    public static MainActivity mainActivity;
    public static boolean shuttingDown = false;

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

    public void allTorrentsActions(View button) {
        switch(button.getId()) {
            case R.id.recheckAllTorrentsButton:
                ConsoleInputHelperFactory.currentCommandReader.recheckTorrents();
                Toast.makeText(this, "Rechecking started", Toast.LENGTH_SHORT).show();
                break;
            case R.id.removeAllTorrentsButton:
                new RemoveTorrentDialog(this).show();
                break;
            case R.id.startAllTorrentsButton:
                ConsoleInputHelperFactory.currentCommandReader.writeLine("s all\n");
                Toast.makeText(this, "All torrents started", Toast.LENGTH_SHORT).show();
                break;
            case R.id.stopAllTorrentsButton:
                ConsoleInputHelperFactory.currentCommandReader.writeLine("h all\n");
                Toast.makeText(this, "All torrents stopped", Toast.LENGTH_SHORT).show();
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
        TorrentStatus ts = torrentAdapter.getItem(pos);
        if(ts==null) {
            Toast.makeText(this, "Torrent index no longer valid", Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.itemRecheck:
                ConsoleInputHelperFactory.currentCommandReader.recheckTorrents(pos+1);
                break;
            case R.id.itemRemove:
                new RemoveTorrentDialog(this,ts.index,ts.origin,ts.path).show();
                break;
            case R.id.itemStart:
                ConsoleInputHelperFactory.currentCommandReader.writeLine("s "+ts.index+"\n");
                break;
            case R.id.itemStop:
                ConsoleInputHelperFactory.currentCommandReader.writeLine("h "+ts.index+"\n");
                break;
            case R.id.itemMove:
                new MoveTorrentDialog(this,ts.index).show();
                break;
            case R.id.itemThrottleSpeeds:
                new ThrottleTorrentDialog(this, ts).show();
                break;
            case R.id.itemLocate:
                File f = new File(ts.path);
                if(!f.isDirectory()) f = f.getParentFile();
                Uri uri = Uri.fromFile(f);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "*/*");
                startActivity(intent);
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

        if(shuttingDown) {
            // old process has not been shutdown yet
            resumeStd = ()->{};
            setContentView(R.layout.info);
            return;
        }

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
            String ml_ = magnetLink.getText().toString();
            if(ml_.startsWith("mgnet.me") || ml_.startsWith("http://mgnet.me")) {
                try {
                    ml_ = Misc.getMagnetLinkFromMgnetMeUrl(ml_);
                    Toast.makeText(this, "Retrieved from mgnet.me:\n"+ml_, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to retrieve magnet link from mgnet.me", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else if (!ml_.startsWith("magnet:") || !(new File(ml_).exists())) {
                Toast.makeText(this, "The provided item is not a magnet link, a torrent file path or a mgnet.me shortened link", Toast.LENGTH_SHORT).show();
                return;
            }
            final String ml = ml_;
            if(ml.isEmpty()) Toast.makeText(MainActivity.this, "Magnet link field is empty", Toast.LENGTH_SHORT).show();
            else {
                magnetLink.setText("");
                toggleAddTorrentLayout(null);
                Toast.makeText(this, "Adding magnet link...", Toast.LENGTH_SHORT).show();
                new Thread(()->{
                    for(int i=0;i<3;i++) {
                        try {
                            ConsoleInputHelperFactory.currentCommandReader.writeLine("a "+ml+"\n");
                            runOnUiThread(()-> Toast.makeText(MainActivity.this, "Magnet link added", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        catch(Exception e) { // should be a NPE, if currentCommandReader is not ready yet
                            e.printStackTrace();
                            LockSupport.parkNanos(1000000000);
                        }
                    }
                    runOnUiThread(()->Toast.makeText(MainActivity.this, "Unable to add magnet link, torrent engine not loaded?", Toast.LENGTH_SHORT).show());
                }).start();
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


    public void startDnsTestActivity(View view) {
        startActivity(new Intent(MainActivity.this,DNSTestActivity.class));
    }
}
