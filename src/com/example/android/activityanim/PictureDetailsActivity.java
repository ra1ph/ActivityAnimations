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

import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import com.example.android.activityanim.widget.PagerAdapter;
import com.example.android.activityanim.widget.TouchImageView;
import com.example.android.activityanim.widget.ViewPager;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * This sub-activity shows a zoomed-in view of a specific photo, along with the
 * picture's text description. Most of the logic is for the animations that will
 * be run when the activity is being launched and exited. When launching,
 * the large version of the picture will resize from the thumbnail version in the
 * main activity, colorizing it from the thumbnail's grayscale version at the
 * same time. Meanwhile, the black background of the activity will fade in and
 * the description will eventually slide into place. The exit animation runs all
 * of this in reverse.
 */
public class PictureDetailsActivity extends Activity {

    private static final Interpolator sDecelerator = new DecelerateInterpolator();
    private static final Interpolator sAccelerator = new AccelerateInterpolator();
    private static final String PACKAGE_NAME = "com.example.android.activityanim";
    private static final int ANIM_DURATION = 500;

    private BitmapDrawable mBitmapDrawable;
    private ColorMatrix colorizerMatrix = new ColorMatrix();
    ColorDrawable mBackground;
    int mLeftDelta;
    int mTopDelta;
    float mWidthScale;
    float mHeightScale;

    private int mOriginalOrientation;
    private int thumbnailTop;
    private int thumbnailLeft;
    private int viewTop;
    private int viewLeft;
    private int padX;
    private int padY;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private CustomPager pager;
    private ImagePagerAdapter adapter;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private ImageView mImageView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) finish();

        setContentView(R.layout.picture_info);

        // Retrieve the data we need for the picture/description to display and
        // the thumbnail to animate it from

        final Bundle bundle = getIntent().getExtras();

        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        int pagerPosition = bundle.getInt(PACKAGE_NAME + ".number");
        thumbnailTop = bundle.getInt(PACKAGE_NAME + ".top");
        thumbnailLeft = bundle.getInt(PACKAGE_NAME + ".left");
        thumbnailWidth = bundle.getInt(PACKAGE_NAME + ".width");
        thumbnailHeight = bundle.getInt(PACKAGE_NAME + ".height");
        mOriginalOrientation = bundle.getInt(PACKAGE_NAME + ".orientation");

        mImageView = (ImageView) findViewById(R.id.image_animation);
        mImageView.setVisibility(View.VISIBLE);
        imageLoader.displayImage(Constants.IMAGES[pagerPosition], mImageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
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
                Toast.makeText(PictureDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                ViewTreeObserver observer = mImageView.getViewTreeObserver();
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        mImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Figure out where the thumbnail and full size versions are, relative
                        // to the screen and each other
                        int[] screenLocation = new int[2];
                        mImageView.getLocationOnScreen(screenLocation);

                        float[] f = new float[9];
                        mImageView.getImageMatrix().getValues(f);

                        final float scaleX = f[Matrix.MSCALE_X];
                        final float scaleY = f[Matrix.MSCALE_Y];

                        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
                        final Drawable d = mImageView.getDrawable();
                        final int origW = d.getIntrinsicWidth();
                        final int origH = d.getIntrinsicHeight();

                        // Calculate the actual dimensions
                        final int actW = Math.round(origW * scaleX);
                        final int actH = Math.round(origH * scaleY);

                        padX = (mImageView.getWidth() - actW) / 2;
                        padY = (mImageView.getHeight() - actH) / 2;

                        mLeftDelta = thumbnailLeft - screenLocation[0] - padX;
                        mTopDelta = thumbnailTop - screenLocation[1] - padY;

                        // Scale factors to make the large version the same size as the thumbnail
                        mWidthScale = (float) thumbnailWidth / actW;
                        mHeightScale = (float) thumbnailHeight / actH;

                        runEnterAnimation();

                        return true;
                    }
                });

            }
        });

        pager = (CustomPager) findViewById(R.id.pager);
        pager.setVisibility(View.GONE);
        adapter = new ImagePagerAdapter(Constants.IMAGES,pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(pagerPosition);
        pager.setPagingEnabled(false);
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    public void runEnterAnimation() {

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up

        Animator scaleX = ObjectAnimator.ofFloat(mImageView, "scaleX", mWidthScale, 1f);
        Animator scaleY = ObjectAnimator.ofFloat(mImageView, "scaleY", mHeightScale, 1f);

        AnimatorProxy.wrap(mImageView).setPivotX(padX);
        AnimatorProxy.wrap(mImageView).setPivotY(padY);

        Animator translationX = ObjectAnimator.ofFloat(mImageView, "translationX", mLeftDelta, 0);
        Animator translationY = ObjectAnimator.ofFloat(mImageView, "translationY", mTopDelta, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(2000);
        animatorSet.playTogether(scaleX, scaleY, translationX, translationY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //To change body of implemented methods use File | Settings | File Templates.
                pager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        animatorSet.start();
    }

    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void runExitAnimation(final Runnable endAction) {
        mImageView.setVisibility(View.VISIBLE);
        pager.setVisibility(View.GONE);

        mImageView.setAlpha(255);
        imageLoader.displayImage(Constants.IMAGES[pager.getCurrentItem()], mImageView, new SimpleImageLoadingListener() {
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
                Toast.makeText(PictureDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                Animator scaleX = ObjectAnimator.ofFloat(mImageView, "scaleX", 1f, mWidthScale);
                Animator scaleY = ObjectAnimator.ofFloat(mImageView, "scaleY", 1f, mHeightScale);

                AnimatorProxy.wrap(mImageView).setPivotX(padX);
                AnimatorProxy.wrap(mImageView).setPivotY(padY);

                Animator translationX = ObjectAnimator.ofFloat(mImageView, "translationX", 0, mLeftDelta);
                Animator translationY = ObjectAnimator.ofFloat(mImageView, "translationY", 0, mTopDelta);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(2000);
                animatorSet.playTogether(scaleX, scaleY, translationX, translationY);
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //To change body of implemented methods use File | Settings | File Templates.
                        endAction.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
                animatorSet.start();

            }
        });
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
        runExitAnimation(new Runnable() {
            public void run() {
                // *Now* go ahead and exit the activity
                finish();
            }
        });
    }

    /**
     * This is called by the colorizing animator. It sets a saturation factor that is then
     * passed onto a filter on the picture's drawable.
     *
     * @param value
     */
    public void setSaturation(float value) {
        colorizerMatrix.setSaturation(value);
        ColorMatrixColorFilter colorizerFilter = new ColorMatrixColorFilter(colorizerMatrix);
        mBitmapDrawable.setColorFilter(colorizerFilter);
    }

    @Override
    public void finish() {
        super.finish();

        // override transitions to skip the standard window animations
        overridePendingTransition(0, 0);
    }


    public class ImagePagerAdapter extends PagerAdapter {

        private String[] images;
        private LayoutInflater inflater;
        private View mCurrentView;
        private CustomPager pager;

        ImagePagerAdapter(String[] images,CustomPager pager) {
            this.images = images;
            inflater = getLayoutInflater();
            this.pager = pager;
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
                    Toast.makeText(PictureDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

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
}
