package com.easeic.elcontrol;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIImage extends CUIBase {
    public boolean mKeepRatio = false;
    Bitmap  mImage = null;
    int     mPage = 0;
    CFunction mFun = null;

    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKDOWN) {
            Rect rc = getRect();
            if (rc.contains((int) param1, (int) param2)) {
                mProject.clickAudio();

                return true;
            }
        }
        if(message == MESSAGE_CLICKUP)
        {
            onClick();
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        if(mImage == null)
            return;
        Rect rc = new Rect(getRect());
        if(mKeepRatio)
        {
            double xScale = rc.width()*1.0/mImage.getWidth();
            double yScale = rc.height()*1./mImage.getHeight();
            double scale = xScale<=yScale?xScale:yScale;
            rc.inset((int)(rc.width()-mImage.getWidth()*scale)/2,(int)(rc.height()-mImage.getHeight()*scale)/2);
        }
        dc.drawBitmap(mImage,new Rect(0,0,mImage.getWidth(),mImage.getHeight()),rc,null);
    }

    static String S_KEEPRATIO = "KeepRatio";;
    static String S_IMAGE = "Image";
    static String S_LINKPAGE = "LinkPage";
    static String S_FUNCTION = "Function";

    @Override
    public void loadXML(Element xmlData) {
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        mKeepRatio = Integer.parseInt(xmlData.getAttribute(S_KEEPRATIO)) == 1;
        if(xmlData.getAttribute(S_IMAGE) != null)
        {
            mImage = WSUtil.loadIOBitmap(mProject.getResFile(xmlData.getAttribute(S_IMAGE)),mRect.width(),mRect.height());
        }
        if(xmlData.getAttribute(S_LINKPAGE) != null && xmlData.getAttribute(S_LINKPAGE).length()>0)
            mPage = Integer.parseInt(xmlData.getAttribute(S_LINKPAGE));
        mFun = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION));
    }

    void onClick()
    {
        mProject.uiSend(mFun);

        if(mPage>0)
            mProject.setActivePage(mPage);
    }
}
