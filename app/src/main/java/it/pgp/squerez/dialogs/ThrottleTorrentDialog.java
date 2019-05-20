package it.pgp.squerez.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import java.util.HashMap;
import java.util.Map;

import it.pgp.squerez.R;
import it.pgp.squerez.responses.TorrentStatus;

public class ThrottleTorrentDialog extends Dialog {

    private final TorrentStatus torrentStatus;

    private final EditText maxUploadSpeed, maxDownloadSpeed;

    private static final Map<Integer,boolean[]> hierarchyMap = new HashMap<Integer,boolean[]>(){{
        put(R.id.increaseDownloadBy50, new boolean[]{true, false, false});
        put(R.id.increaseDownloadBy100, new boolean[]{true, false, true});
        put(R.id.decreaseDownloadBy50, new boolean[]{false, false, false});
        put(R.id.decreaseDownloadBy100, new boolean[]{false, false, true});
        put(R.id.increaseUploadBy50, new boolean[]{true, true, false});
        put(R.id.increaseUploadBy100, new boolean[]{true, true, true});
        put(R.id.decreaseUploadBy50, new boolean[]{false, true, false});
        put(R.id.decreaseUploadBy100, new boolean[]{false, true, true});
    }};

    private void sendThrottleCommand(boolean forUpload, String speedKbps) {
        ConsoleInputHelperFactory.currentCommandReader.writeLine("hack "+
                torrentStatus.index+
                (forUpload?" uploadspeed ":" downloadspeed ")+
                (speedKbps.isEmpty()?"0":speedKbps)+"\n");
    }

    private void increaseOrDecreaseSpeed(View v) {
        boolean[] opts = hierarchyMap.get(v.getId());
        EditText toEdit = opts[1]?maxUploadSpeed:maxDownloadSpeed;
        int speedToChange = (opts[0]?1:-1)*(opts[2]?100:50);
        String currentSpeed = toEdit.getText().toString();
        int targetSpeed = (currentSpeed.isEmpty()?0:Integer.parseInt(currentSpeed)) + speedToChange;
        if (targetSpeed < 0) targetSpeed = 0;
        toEdit.setText(""+targetSpeed);
    }

    public ThrottleTorrentDialog(Context context, TorrentStatus torrentStatus) {
        super(context);
        setContentView(R.layout.throttle_dialog);
        this.torrentStatus = torrentStatus;
        maxUploadSpeed = findViewById(R.id.maxUploadSpeed);
        maxDownloadSpeed = findViewById(R.id.maxDownloadSpeed);
        maxUploadSpeed.setText(""+(torrentStatus.maxUpBps/1000));
        maxDownloadSpeed.setText(""+(torrentStatus.maxDownBps/1000));
        findViewById(R.id.throttleSpeedOkButton).setOnClickListener(v->{
            sendThrottleCommand(true,maxUploadSpeed.getText().toString());
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            sendThrottleCommand(false,maxDownloadSpeed.getText().toString());
            dismiss();
        });
        for(int id : new Integer[]{
                R.id.increaseDownloadBy50,R.id.increaseDownloadBy100,
                R.id.decreaseDownloadBy50,R.id.decreaseDownloadBy100,
                R.id.increaseUploadBy50,R.id.increaseUploadBy100,
                R.id.decreaseUploadBy50,R.id.decreaseUploadBy100}) {
            findViewById(id).setOnClickListener(this::increaseOrDecreaseSpeed);
        }
    }
}
