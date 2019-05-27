package it.pgp.squerez.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import it.pgp.squerez.R;

public class AboutDialog extends Dialog {

    public void styleIt() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window w = getWindow();
        w.setBackgroundDrawable(new ColorDrawable(0x8000C000));
    }

    public AboutDialog(Context context) {
        super(context,R.style.CustomDialog);
        styleIt();
        setContentView(R.layout.about_dialog);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ((TextView)(findViewById(R.id.aboutAppVersionName))).setText(pInfo.versionName);
            ((TextView)(findViewById(R.id.aboutAppVersionCode))).setText(""+pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
