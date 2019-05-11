package it.pgp.squerez.service.visualization;

// imported/adapted from XFiles

import android.os.Build;
import android.view.WindowManager;

public enum ViewType {
    CONTAINER,
    ANCHOR;

    public static final int OVERLAY_WINDOW_TYPE = (Build.VERSION.SDK_INT < 26)?
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT:
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
}

