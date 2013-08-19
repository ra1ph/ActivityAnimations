package com.example.android.activityanim.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.android.activityanim.CustomPager;
import com.example.android.activityanim.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 19.08.13
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;
    private String[] images;
    private LayoutInflater inflater;
    private View mCurrentView;
    private CustomPager pager;
    private ImageView mImageView;

    public ImagePagerAdapter(Context context, String[] images, CustomPager pager, ImageView mImageView) {
        this.images = images;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pager = pager;
        this.mImageView = mImageView;

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @Override
    public void finishUpdate(View container) {
    }

    @Override
    public int getCount() {
        return images.length;
    }


    @Override
    public Object instantiateItem(View view, int position) {

        View imageLayout = inflater.inflate(R.layout.item_pager_image, (ViewGroup) view, false);
        TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
        final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
        imageView.setPager(pager);

        imageLoader.displayImage(images[position], imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                //spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }

                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
                mImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //To change body of implemented methods use File | Settings | File Templates.
                        mImageView.setVisibility(View.INVISIBLE);
                        mImageView.invalidate();
                        mImageView.setAlpha(0);
                        pager.setPagingEnabled(true);
                    }
                }, 500);


            }
        });

        ((ViewPager) view).addView(imageLayout, 0);
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View container) {
    }


    @Override
    public void setPrimaryItem(View container, int position, Object object) {
        super.setPrimaryItem(container, position, object);    //To change body of overridden methods use File | Settings | File Templates.
        mCurrentView = (View)object;
    }

    public View getCurrentView() {
        return mCurrentView;
    }
}
