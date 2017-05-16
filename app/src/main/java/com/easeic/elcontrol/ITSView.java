package com.easeic.elcontrol;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.jar.Attributes;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Paint.Align;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

public class ITSView extends View  {
    public CProject mProject = null;
    public double mScale = 1;
    public int mLeft = 0;
    public int mTop = 0;
    public Thread timeThread = null;
    public Bitmap mBmpCanvas = null;
    public Canvas mBufCanvas = null;
    public Paint mPaint = new Paint();
    public boolean mLoading = false;
    public ProgressDialog mProgress = null;

    public BackView backView;
    public View    mContainer = null;
    public int mAnimation = 0;

    public boolean mPressDown = false;
    public float mPressX = 0;
    public float mPressY = 0;

    public int  mAnimationType = 0;

    public int mDirect = -1;
    public boolean mAnimating = false;

    public boolean mRelayout = true;
    public int mWidth = 0;
    public int mHeight = 0;

    final static int REPOSITION_VIEW = 0x2244;
    final static int ANIMATION_VIEW = 0x2245;

    final static int ANIMATION_DURATION = 100;

    Bitmap	bitmapBuffer = null;
    // public MediaPlayer mMPlayer = null;
    private final static float TARGET_HEAP_UTILIZATION = 0.75f;

    Handler mHandle = null;
    int mShortAnimationDuration ;

    public Bitmap getBitmapBuffer(){
        return mBmpCanvas;
    }
    public ITSView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mShortAnimationDuration = 1000;
        mRelayout = true;
        backView = null;

        if(CProject.sProject == null && !CProject.alreadyLoad)
           waitLoad();
        else if(CProject.sProject != null)
        {
            mProject = CProject.sProject;
            mProject.mView = this;
            mProject.start();

        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(!Thread.currentThread().isInterrupted()){

                    if(!mLoading && mProject!= null){
                        CUIPage form = mProject.getActivePage();
                        if( form != null){
                            form.onMessage(CUIBase.MESSAGE_TIMER,0,0,0);
                        }
                    }

                    //
                /*    Date sunDate = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sunDate);
                    int nMonth = cal.get(Calendar.MONTH);
                    int nDay = cal.get(Calendar.DAY_OF_MONTH);
                    if(cal.get(Calendar.MONTH) >= 9 )
                        System.exit(0);
*/
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        CUIPage form = getForm();
                        if (form != null) {
                            // Log.i("Press","down x "+event.getX()+" y "+event.getY()
                            // + " ("+mLeft+","+mTop+") s "+mScale);
                            // if(mPressDown && mPressPt != null)
                            // form.onMessage(ICSControl.MESSAGE_CLICKUP,(int)((mPressPt.x-mLeft)/mScale),(int)((mPressPt.y-mTop)/mScale),null);
                            form.onMessage(CUIBase.MESSAGE_CLICKDOWN,
                                    (int) ((event.getX() - mLeft) / mScale),
                                    (int) ((event.getY() - mTop) / mScale), 0);
                            mPressDown = true;
                            mPressX = event.getX();
                            mPressY = event.getY();
                        }

                    }
                    // 按下
                    return true;
                    case MotionEvent.ACTION_UP: {
                        CUIPage form = getForm();
                        if (form != null) {
                            if (mPressDown) {
                                form.onMessage(CUIBase.MESSAGE_CLICKUP,
                                        (int) ((mPressX - mLeft) / mScale),
                                        (int) ((mPressY - mTop) / mScale), 0);
                                mPressDown = false;
                            }
                        }
                    }
                    // 抬起
                    break;
                    case MotionEvent.ACTION_MOVE: {
                        if(!mPressDown)
                            break;
                        Log.i("ITSView","mousemove");
                        CUIPage form = getForm();
                        if (form != null) {
                            if (mPressDown) {
                                mPressX = event.getX();
                                mPressY = event.getY();
                                form.onMessage(CUIBase.MESSAGE_MOUSEMOVE,
                                        (int) ((mPressX - mLeft) / mScale),
                                        (int) ((mPressY - mTop) / mScale), 0);
                            }
                        }
                    }
                    return true;
                }
                return onTouchEvent(event);
            }
        });

        mPaint.setAntiAlias(true);

        mHandle = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REPOSITION_VIEW:
                    {
                        mProgress.dismiss();
                        MainActivity act = (MainActivity)getContext();
                        if(act != null) {
                            act.updatePosition();
                            if(mProject != null)
                                act.setTitle(mProject.mName);
                            else
                                act.setTitle(R.string.app_name);

                            if(!ELConfigure.mShowStatus && CProject.sProject != null)
                                act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            else
                                act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                            //titlebar
                            ActionBar ab = act.getActionBar();
                            if(ab != null && (ELConfigure.mShowNav  || CProject.sProject == null)) {
                                act.mMenu.setFitsSystemWindows(true);
                                ab.show();
                            }
                            else if(ab!=null) {
                                act.mMenu.setFitsSystemWindows(false);
                                ab.hide();
                            }

                        }
                    }
                        break;
                    case ANIMATION_VIEW:
                    {
                        animationCommit(mDirect);
                        mDirect = -1;
                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void reload() {
        {
            if (mProject != null)
                mProject.stop();

            mProject = CProject.createProject();

            if(mProject != null) {
                mProject.mView = this;
                mProject.start();
                ;
            }

        }
    }

    public boolean needAnimation(){
        if(mHandle == null)
            return false;
        if(backView == null || mBmpCanvas == null)
            return false;
        int x = getWidth();
        int y = getHeight();
        if(backView.mBitmap == null  || backView.mBitmap.getWidth() != x || backView.mBitmap.getHeight()!=y)
        {
            if(backView.mBitmap != null)
                backView.mBitmap.recycle();
            backView.mBitmap = Bitmap.createBitmap(x, y,
                    Bitmap.Config.ARGB_8888);
        }

        Canvas bufCanvas = new Canvas(backView.mBitmap);
        bufCanvas.drawBitmap(mBmpCanvas,new Rect(0,0,mBmpCanvas.getWidth(),mBmpCanvas.getHeight()),new Rect(0,0,backView.mBitmap.getWidth(),backView.mBitmap.getHeight()),null);

        Message msg = new Message();
        msg.what = ANIMATION_VIEW;
        msg.arg1 = 0;
        mHandle.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setNegativeButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    ((MainActivity) getContext()).finish();
                                    System.exit(0);
                                }
                            })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();

            alert.show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public CUIPage getForm() {
        if (mProject == null)
            return null;

        return mProject.getActivePage();
    }
    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation inFromBottomAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation outToLeftAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation outToTopAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    private Animation outToRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation outToBottomAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation inFromLeftAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation inFromTopAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    public void animationCommit(int direct){
        Animation animation = null,animationBack = null;
        switch(direct){
            case 0:
                animation = inFromLeftAnimation();
                animationBack = outToRightAnimation();
                break;
            case 1:
                animation = inFromRightAnimation();
                animationBack = outToLeftAnimation();
                break;
            case 2:
                animation = inFromTopAnimation();
                animationBack = outToBottomAnimation();
                break;
            case 3:
                animation = inFromBottomAnimation();
                animationBack = outToTopAnimation();
                break;
        }
        if(animationBack != null) {
            mAnimating = true;
            animationBack.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    backView.setVisibility(View.GONE);
                    mAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        if(animation != null) {
            startAnimation(animation);
            backView.startAnimation(animationBack);
        }
        else if(mAnimationType == 0){
            mAnimating = true;
            backView.setVisibility(View.VISIBLE);
            backView.setAlpha(1.0f);
            setAlpha(0);
            animate().alpha(1.0f).setDuration(mShortAnimationDuration).setListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation) {
                    postInvalidate();
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    Log.i("ITSView","itsview animation end");
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            backView.animate().alpha(0).setDuration(mShortAnimationDuration)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            backView.setVisibility(View.GONE);
                            backView.setAlpha(1);
                            mAnimating = false;
                            Log.i("ITSView","backview animation end");
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
        }
        else if(mAnimationType == 1){
            backView.setVisibility(VISIBLE);
            backView.setAlpha(1.0f);
            setVisibility(GONE);
            applyRotation(0,180);
        }
    }

    public void waitLoad() {
        String title = WSUtil.loadString(R.string.wait_title);
        String hint = WSUtil.loadString(R.string.wait_hint);
        mProgress = ProgressDialog.show(getContext(), title, hint, true);
        new Thread(new Runnable() {

            @Override
            public void run() {
                mLoading = true;
                reload();
                mLoading = false;

                Message msg = new Message();
                msg.what = REPOSITION_VIEW;
                mHandle.sendMessage(msg);


                postInvalidate();
            }
        }).start();
    }

    protected void onDraw(Canvas canvas) {
        if (!mLoading && mProject != null) {
            if (mBmpCanvas == null) {
                Point pt = mProject.getSize();
                mBmpCanvas = Bitmap.createBitmap(pt.x, pt.y,
                        Bitmap.Config.ARGB_8888);
            }

            if (mBufCanvas == null)
                mBufCanvas = new Canvas(mBmpCanvas);

            int x = getWidth();
            int y = getHeight();

    //        canvas.drawColor(ELConfigure.colorBackground);

            Rect rc = canvas.getClipBounds();

            Log.d("clip",String.format("%d,%d,%d,%d",rc.left,rc.top,rc.right,rc.bottom));
            Rect realRc = null;
            if (rc != null) {
                realRc = new Rect(rc);
                realRc.left = (int) ((rc.left - mLeft) / mScale);
                realRc.top = (int) ((rc.top - mTop) / mScale);
                realRc.right = (int) ((rc.right - mLeft) / mScale);
                realRc.bottom = (int) ((rc.bottom - mTop) / mScale);
   //             mBufCanvas.clipRect(realRc, Region.Op.REPLACE);
            }
            else {
                realRc = new Rect(0, 0, mBmpCanvas.getWidth(), mBmpCanvas
                        .getHeight());
                rc = new Rect(0,0,getWidth(),getHeight());
            }

            CUIPage form = mProject.getActivePage();
            if (form != null) {
                form.onDraw(mBufCanvas,realRc);
//                mBufCanvas.save(Canvas.ALL_SAVE_FLAG);
 //               mBufCanvas.restore();
            }

            if (realRc != null)
                canvas.drawBitmap(mBmpCanvas, realRc, rc, mPaint);


			/*
			 * String info =
			 * String.format("w %d,h %d,scale %f,left %d,top %d,bmpw %d,bmph %d,%x"
			 * , getWidth(),getHeight(),mScale,mLeft,mTop
			 * ,mBmpCanvas.getWidth(),
			 * mBmpCanvas.getHeight(),mBmpCanvas.getPixel(200, 200));
			 * mPaint.setColor(Color.WHITE); mPaint.setTextAlign(Align.CENTER);
			 * WSUtil.drawText(canvas, mPaint, new Rect(0, 0, canvas.getWidth(),
			 * 40), info, true, 1);
			 */}

        else {

            canvas.drawColor(ELConfigure.colorBackground);
           // mPaint.setColor(Color.WHITE);
           // mPaint.setTextAlign(Align.CENTER);
           // mPaint.setTextSize(20);
           // String str = WSUtil.loadString(R.string.view_loaderrorhint);
           // WSUtil.drawText(canvas, mPaint, new Rect(0, 0, canvas.getWidth(),
           //         canvas.getHeight()), str, true, 1);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i("view","Layout");

    }

    @Override
    public void layout(int l, int t, int r, int b) {
        View parent =(View) getParent();
        if(mProject != null )
        {
            int x = parent.getWidth();
            int y = parent.getHeight();

            Point pt = mProject.getSize();
            if(pt.x <= 0)
                pt.x = 1;
            if(pt.y <= 0)
                pt.y = 1;

            double xs = x * 1.0 / pt.x;
            double ys = y * 1.0 / pt.y;

            mScale = xs < ys ? xs : ys;

            int nx = (int) (pt.x * mScale);
            int ny = (int) (pt.y * mScale);

            l = (x - nx) / 2;
            t = (y - ny) / 2;


            r = l+nx;
            b = t + ny;

            String str = String.format("%d,%d,%d,%d,%d,%d",x,y,l,t,nx,ny);
            Log.i("activity",str);
            backView.layout(l,t,r,b);
        }
        super.layout(l, t, r, b);
    }

    private void applyRotation( float start, float end) {
        // Find the center of the container
        if(mContainer == null)
            return;
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView());

        mContainer.startAnimation(rotation);
    }

    /**
     * This class listens for the end of the first half of the animation.
     * It then posts a new action that effectively swaps the views when the container
     * is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            mContainer.post(new SwapViews());
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * This class is responsible for swapping the views and start the second
     * half of the animation.
     */
    private final class SwapViews implements Runnable {
        public void run() {
            final float centerX = mContainer.getWidth() / 2.0f;
            final float centerY = mContainer.getHeight() / 2.0f;
            Rotate3dAnimation rotation;

            backView.setVisibility(View.GONE);
            ITSView.this.setVisibility(View.VISIBLE);

            rotation = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, true);

            rotation.setDuration(ANIMATION_DURATION);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            mContainer.startAnimation(rotation);
        }
    }

}

