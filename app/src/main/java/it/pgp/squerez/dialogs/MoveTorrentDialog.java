package it.pgp.squerez.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;

import it.pgp.squerez.R;

public class MoveTorrentDialog extends Dialog {

    public MoveTorrentDialog(Context context, final int torrentIndex) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.move_dialog);
        findViewById(R.id.moveTorrentOKButton).setOnClickListener(v->{
            String x = ((EditText)findViewById(R.id.moveTorrentEditText)).getText().toString();
            if(x.isEmpty()) {
                Toast.makeText(context, "No destination position provided", Toast.LENGTH_SHORT).show();
                return;
            }
            ConsoleInputHelperFactory.currentCommandReader.writeLine("m "+torrentIndex+" "+x+"\n");
            dismiss();
        });
    }
}
