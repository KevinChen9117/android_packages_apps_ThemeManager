package com.android.thememanager;

/**
 * Created with IntelliJ IDEA.
 * User: lithium
 * Date: 11/23/12
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PreviewManager {
    private final Map<String, BitmapDrawable> drawableMap;

    public PreviewManager() {
        drawableMap = new HashMap<String, BitmapDrawable>();
    }

    public BitmapDrawable fetchDrawable(String themId) {
        if (drawableMap.containsKey(themId)) {
            return drawableMap.get(themId);
        }

        Log.d(this.getClass().getSimpleName(), "theme ID:" + themId);
        try {
            InputStream is = fetch(themId);
            BitmapDrawable drawable = (BitmapDrawable)BitmapDrawable.createFromStream(is, "src");


            if (drawable != null) {
                drawableMap.put(themId, drawable);
                Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
                Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
            }

            return drawable;
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final String themId, final ImageView imageView) {
        if (drawableMap.containsKey(themId)) {
            imageView.setImageDrawable(drawableMap.get(themId));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageDrawable((BitmapDrawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                BitmapDrawable drawable = fetchDrawable(themId);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private ZipInputStream fetch(String themId) throws IOException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/" +
                Globals.THEME_PATH + "/" + themId);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().equals("preview/preview_launcher_0.png"))
                return zis;
        }

        return null;
    }
}