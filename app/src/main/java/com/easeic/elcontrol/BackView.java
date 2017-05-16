package com.easeic.elcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sam on 2016/6/9.
 */
public class BackView extends View {
    public Bitmap   mBitmap = null;
    public BackView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mBitmap != null)
        {
            canvas.drawBitmap(mBitmap,0,0,null);
        }
    }

}
