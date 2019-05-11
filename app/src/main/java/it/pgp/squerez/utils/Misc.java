package it.pgp.squerez.utils;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Misc {

    public static File getAvailableFileName(File parentDir) {
        int i = 0;
        File f;
        do {
            f = new File(parentDir,""+i++);
        } while (f.exists());
        return f;
    }

    public static String dumpContentUriIntoTmpFile(Context context, Uri contentUri, File parentDir) throws IOException {
        File f = getAvailableFileName(parentDir); // TODO overwrite existing, use always the same name
        // opening InputStream from FileDescriptor is working as well
//        FileDescriptor fd = context.getContentResolver().openFileDescriptor(contentUri,"r").getFileDescriptor();
//        try(InputStream is = new FileInputStream(fd);
        try(InputStream is = context.getContentResolver().openInputStream(contentUri);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            byte[] x = new byte[1048576];
            for(;;) {
                int readBytes = is.read(x);
                if (readBytes <= 0) break;
                os.write(x,0,readBytes);
            }
        }
        return f.getAbsolutePath();
    }
}
