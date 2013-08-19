/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.activityanim;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * This example shows how to create a custom activity animation when you want something more
 * than window animations can provide. The idea is to disable window animations for the
 * activities and to instead launch or return from the sub-activity immediately, but use
 * property animations inside the activities to customize the transition.
 * <p/>
 * Watch the associated video for this demo on the DevBytes channel of developer.android.com
 * or on the DevBytes playlist in the androiddevelopers channel on YouTube at
 * https://www.youtube.com/playlist?list=PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0.
 */
public class ActivityAnimations extends Activity {

    private static final String PACKAGE = "com.example.android.activityanim";
    static float sAnimatorScale = 1;

    GridView mGridLayout;
    HashMap<ImageView, PictureData> mPicturesData = new HashMap<ImageView, PictureData>();
    private String[] imageUrls;
    private DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        Bundle bundle = getIntent().getExtras();
        imageUrls = Constants.IMAGES;
        initImageLoader(this);

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory()
                .cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        mGridLayout = (GridView) findViewById(R.id.gridview);
        mGridLayout.setNumColumns(3);
        mGridLayout.setAdapter(new ImageAdapter());

        mGridLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {

                screenLocation = new int[2];

                v.getLocationInWindow(screenLocation);

                ImageView image = (ImageView) v.findViewById(R.id.image);
                float[] f = new float[9];
                image.getImageMatrix().getValues(f);

                final float scaleX = f[Matrix.MSCALE_X];
                final float scaleY = f[Matrix.MSCALE_Y];

                // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
                final Drawable d = image.getDrawable();
                final int origW = d.getIntrinsicWidth();
                final int origH = d.getIntrinsicHeight();

                // Calculate the actual dimensions
                final int actW = Math.round(origW * scaleX);
                final int actH = Math.round(origH * scaleY);

                viewWidth = v.getWidth();
                viewHeight = v.getHeight();

                info = mPicturesData.get(v);
                Intent subActivity = new Intent(ActivityAnimations.this,
                        PictureDetailsActivity.class);
                orientation = getResources().getConfiguration().orientation;
                subActivity.
                        putExtra(PACKAGE + ".number", i).
                        putExtra(PACKAGE + ".orientation", orientation).
                        putExtra(PACKAGE + ".left", screenLocation[0] + (v.getWidth() - actW)/2).
                        putExtra(PACKAGE + ".top", screenLocation[1] + (v.getHeight() - actH)/2).
                        putExtra(PACKAGE + ".width", actW).
                        putExtra(PACKAGE + ".height", actH);

                Log.d("myLog", "X: " + screenLocation[0] + " Y: " + screenLocation[1]);

                Log.d("myLog", "ImageView  X: " + v.getWidth() + " Y: " + v.getHeight());

                startActivity(subActivity);

                // Override transitions: we don't want the normal window animation in addition
                // to our custom one
                overridePendingTransition(0, 0);
            }
        });

    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .enableLogging() // Not necessary in common
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_better_window_animations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_slow) {
            sAnimatorScale = item.isChecked() ? 1 : 5;
            item.setChecked(!item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private int[] screenLocation;
    private int viewWidth;
    private int viewHeight;
    private PictureData info;
    private int orientation;
    /**
     * When the user clicks a thumbnail, bundle up information about it and launch the
     * details activity.
     */
    private View.OnClickListener thumbnailClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Interesting data to pass across are the thumbnail size/location, the
            // resourceId of the source bitmap, the picture description, and the
            // orientation (to avoid returning back to an obsolete configuration if
            // the device rotates again in the meantime)
            screenLocation = new int[2];
            v.getLocationInWindow(screenLocation);

            viewWidth = v.getWidth();
            viewHeight = v.getHeight();

            info = mPicturesData.get(v);
            Intent subActivity = new Intent(ActivityAnimations.this,
                    PictureDetailsActivity.class);
            orientation = getResources().getConfiguration().orientation;
            subActivity.
                    putExtra(PACKAGE + ".orientation", orientation).
                    putExtra(PACKAGE + ".resourceId", info.resourceId).
                    putExtra(PACKAGE + ".left", screenLocation[0]).
                    putExtra(PACKAGE + ".top", screenLocation[1]).
                    putExtra(PACKAGE + ".width", v.getWidth()).
                    putExtra(PACKAGE + ".height", v.getHeight()).
                    putExtra(PACKAGE + ".description", info.description);


            startActivity(subActivity);

            // Override transitions: we don't want the normal window animation in addition
            // to our custom one
            overridePendingTransition(0, 0);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }


    public class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            imageLoader.displayImage(imageUrls[position], imageView, options);

            return imageView;
        }
    }

}
