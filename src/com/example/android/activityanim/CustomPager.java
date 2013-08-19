package com.example.android.activityanim;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.example.android.activityanim.widget.TouchImageView;
import com.example.android.activityanim.widget.ViewPager;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 19.08.13
 * Time: 10:30
 * To change this template use File | Settings | File Templates.
 */
public class CustomPager extends ViewPager implements GestureDetector.OnGestureListener{
    private boolean isPagingEnabled = true;
    GestureDetector gestureDetector = new GestureDetector(this);

    public CustomPager(Context context) {
        super(context);
    }

    public CustomPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isPagingEnabled)
            return false;

        TouchImageView image = (TouchImageView) ((PictureDetailsActivity.ImagePagerAdapter)getAdapter()).getCurrentView().findViewById(R.id.image);
        if(image.isInBounds())
            return false;
            else return super.onInterceptTouchEvent(ev);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean isPagingEnabled() {
        return isPagingEnabled;
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        isPagingEnabled = pagingEnabled;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        TouchImageView image = (TouchImageView) ((PictureDetailsActivity.ImagePagerAdapter)getAdapter()).getCurrentView().findViewById(R.id.image);
        if(image.isInBounds())
            requestDisallowInterceptTouchEvent(true); //To change body of implemented methods use File | Settings | File Templates.
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        Log.d("myLog","Fling!");
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
