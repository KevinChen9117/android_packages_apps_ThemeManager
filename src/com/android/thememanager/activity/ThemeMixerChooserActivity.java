/*
 * Copyright (C) 2012 The ChameleonOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.thememanager.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.IThemeManagerService;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.thememanager.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public class ThemeMixerChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private GridView mGridView = null;
    private List<Theme> mThemeList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private int mElementType = Theme.THEME_ELEMENT_TYPE_ICONS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);

        mElementType = getIntent().getIntExtra("type", 0);
        mThemeList = themeList(mElementType);

        setTitle(Theme.sElementLabels[mElementType]);

        mGridView = (GridView) findViewById(R.id.coverflow);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                if (mElementType != Theme.THEME_ELEMENT_TYPE_BOOTANIMATION) {
                    intent = new Intent(ThemeMixerChooserActivity.this, ThemeElementDetailActivity.class);
                } else {
                    intent = new Intent(ThemeMixerChooserActivity.this, ThemeBootanimationDetailActivity.class);
                }
                intent.putExtra("type", mElementType);
                intent.putExtra("theme_id", mThemeList.get(i).getId());
                startActivity(intent);
            }
        });
        mAdapter = new ImageAdapter(ThemeMixerChooserActivity.this);
        mGridView.setAdapter(mAdapter);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        mAdapter = null;
        mGridView = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private List<Theme> themeList(int elementType) {
        ThemesDataSource dataSource = new ThemesDataSource(this);
        dataSource.open();
        List<Theme> list = null;
        switch(elementType) {
            case Theme.THEME_ELEMENT_TYPE_ICONS:
                list = dataSource.getIconThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                list = dataSource.getWallpaperThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                list = dataSource.getSystemUIThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                list = dataSource.getFrameworkThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                list = dataSource.getLockscreenThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                list = dataSource.getRingtoneThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                list = dataSource.getBootanimationThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_MMS:
                list = dataSource.getMmsThemes();
                break;
        }

        dataSource.close();
        return list;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_theme_element, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_reset:
                String installedThemeDir = "/data/system/theme/";
                try {
                    IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                    switch (mElementType) {
                        case Theme.THEME_ELEMENT_TYPE_ICONS:
                            ts.resetThemeIcons();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                            ts.resetThemeWallpaper();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                            ts.resetThemeSystemUI();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                            ts.resetThemeFramework();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                            ts.resetThemeLockscreen();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                            ts.resetThemeRingtones();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                            ts.resetThemeBootanimation();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_MMS:
                            ts.resetThemeMms();
                            break;
                    }
                } catch (Exception e) {
                }
                return true;
            default:
                return false;
        }
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;

        private ElementPreviewManager mPreviewManager = new ElementPreviewManager();

        private ImageView[] mImages;
        private int mPreviewWidth;
        private int mPreviewHeight;

        public ImageAdapter(Context c) {
            mContext = c;
            DisplayMetrics dm = c.getResources().getDisplayMetrics();
            mPreviewWidth = dm.widthPixels / 3;
            mPreviewHeight = dm.heightPixels / 3;
        }

        public int getCount() {
            return mThemeList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.theme_preview, null);
                FrameLayout fl = (FrameLayout)v.findViewById(R.id.preview_layout);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)fl.getLayoutParams();
                params.width = mPreviewWidth;
                params.height = mPreviewHeight;
                fl.setLayoutParams(params);
            }
            ImageView i = (ImageView)v.findViewById(R.id.preview_image);//mImages[position];//new ImageView(mContext);
            if (i.getDrawable() == null) {
                i.setImageResource(R.drawable.preview);
            }

            mPreviewManager.fetchDrawableOnThread(mThemeList.get(position), mElementType, i);

            TextView tv = (TextView) v.findViewById(R.id.theme_name);
            tv.setText(mThemeList.get(position).getTitle());

            if (mThemeList.get(position).getIsCosTheme())
                v.findViewById(R.id.miui_indicator).setVisibility(View.GONE);
            else
                v.findViewById(R.id.miui_indicator).setVisibility(View.VISIBLE);

            return v;
        }

        public void destroy() {
            /*
            for (int i = 0; i < mImages.length; i++) {
                if (mImages[i] != null && mImages[i].getDrawable() != null) {
                    mImages[i].getDrawable().setCallback(null);
                    mImages[i].setImageDrawable(null);
                }
            }
            */
            mPreviewManager = null;
            mContext = null;
        }
    }
}
