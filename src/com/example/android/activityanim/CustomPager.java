package com.example.android.activityanim;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.example.android.activityanim.widget.ImagePagerAdapter;
import com.example.android.activityanim.widget.TouchImageView;
import com.example.android.activityanim.widget.ViewPager;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 19.08.13
 * Time: 10:30
 * To change this template use File | Settings | File Templates.
 */
public class CustomPager extends ViewPager{
    private boolean isPagingEnabled = true;
    private VelocityTracker mVelocityTracker;

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
        TouchImageView image = (TouchImageView) getAdapter().getCurrentView().findViewById(R.id.image);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
                velocityTracker, mActivePointerId);

        if((ev.getAction() == MotionEvent.ACTION_UP) || (ev.getAction() == MotionEvent.ACTION_CANCEL)) mVelocityTracker = null;

        if(initialVelocity!=0 && image.isInBounds(initialVelocity))
            return false;
            else return super.onInterceptTouchEvent(ev);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean isPagingEnabled() {
        return isPagingEnabled;
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        isPagingEnabled = pagingEnabled;
    }

}
