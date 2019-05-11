package it.pgp.squerez;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

public class PermissionManagementActivity extends Activity {

    public enum PermReqCodes { STORAGE, SYSTEM_SETTINGS, OVERLAYS, STORAGE_READ /*, EXTERNAL_SD*/ }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermReqCodes.STORAGE.ordinal()) {
            if (grantResults.length == 0) { // request cancelled
                Toast.makeText(this, "Storage permissions denied", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permissions denied", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show();

            requestStorageReadPermissions(); // Oreo and above needs this!
        }
        else if (requestCode == PermReqCodes.STORAGE_READ.ordinal()) {
            if (grantResults.length == 0) { // request cancelled
                Toast.makeText(this, "Storage READ permissions denied", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage READ permissions denied", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, "Storage READ permissions granted", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PermReqCodes prc = PermReqCodes.values()[requestCode];
        switch (prc) {
            case STORAGE:
                Toast.makeText(this, "Nothing to do here, already handled in onRequestPermissionsResult", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void requestStoragePermissions(View unused) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PermReqCodes.STORAGE.ordinal());
    }

    // for Oreo, that absurdly needs READ external storage permission request AFTER WRITE one has already been granted (and the latter in this case is automatically granted!)
    protected void requestStorageReadPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PermReqCodes.STORAGE_READ.ordinal());
    }

    public void completePermissions(View unused) {
        SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
        editor.putBoolean("1stRun", false);
        editor.apply();

        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("First run configuration");
        setContentView(R.layout.activity_permission_management);
    }
}
