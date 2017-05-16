package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/25.
 */
public class CUISlider extends CUIBase {
    public CFunction mFun;
    public int  mRangeMax = 100;
    public int  mRangeMin = 0;
    public int  colorSlotBorder = 0;
    public int  colorSlotBackground = 0;
    public int  colorSlotForeground = 0;
    public int  mSlotStyle = 1;
    //thrumb
    public int  mThumbLength = 20;
    public int  colorThumbBorder = 0;
    public int  colorThumbBackground = 0;
    public int  mThumbStyle = 1;
    //feedback
    public int  mBus = 0;
    public int  mArea = 0;
    public int  mCh = 0;

    public int  mTempType = 1;

    public int  mValue = 0;
    public boolean mPress = false;
    public int  mStyle = 0;
    public Rect rectThumb;
    public boolean mActive = false;
    public long mTick = 0;

    private int darkColor = 0xff323232;
    private int lightColor = 0xffe0e0e0;

    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKDOWN)
        {
            Rect rc = new Rect(getRect());

            rc.inset(-2,-2);
            if(rc.width()<=0 || rc.height()<=0 || !rc.contains((int)param1,(int)param2))
                return false;

            rc.inset(2,2);
            mProject.clickAudio();
            mActive = true;
            if(mRangeMax>mRangeMin)
            {
                if(rc.width()>=rc.height())
                    mValue = (int)((param1-rc.left)*1./rc.width()*(mRangeMax-mRangeMin)+mRangeMin);
                else
                    mValue = (int)((rc.bottom-param2)*1.0/rc.height()*(mRangeMax-mRangeMin)+mRangeMin);
                if(mValue>mRangeMax)
                    mValue = mRangeMax;
                else if(mValue<mRangeMin)
                    mValue = mRangeMin;
                onClick();
                mPress = true;
            }

            mTick = System.currentTimeMillis();

            updateUI();;
            return true;
        }
        else if(message == MESSAGE_CLICKUP)
        {
            Rect rc = getRect();
            mActive = false;
            mPress = false;
            if(!rc.contains((int)param1,(int)param2))
                return false;
            updateUI();;

            return true;
        }
        else if(message == MESSAGE_MOUSEMOVE)
        {
            Log.i("slider","slider enter hover");
            if(!mPress || (System.currentTimeMillis()-mTick)<60) {
                if(!mPress)
                    Log.i("slider","not press");
                else
                    Log.i("slider","have nore time");
                return false;
            }
            mTick = System.currentTimeMillis();
            Rect rc = new Rect(getRect());
            rc.inset(-20,-20);
            if(rc.width()<0 || rc.height()<0 || !rc.contains((int)param1,(int)param2))
            {
                Log.i("slider","slider not hover");
                mPress = false;
                return false;
            }
            rc.inset(20,20);
            if(rc.width()>=rc.height())
                mValue = (int)((param1-rc.left)*1./rc.width()*(mRangeMax-mRangeMin)+mRangeMin);
            else
                mValue = (int)((rc.bottom-param2)*1.0/rc.height()*(mRangeMax-mRangeMin)+mRangeMin);
            if(mValue>mRangeMax)
                mValue = mRangeMax;
            else if(mValue<mRangeMin)
                mValue = mRangeMin;
            onClick();
            updateUI();;

            Log.i("Slider","slider hover");
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        Rect rc = getRect();
        if(mRangeMax<=mRangeMin)
            return;
        if(mValue<mRangeMin)
            mValue = mRangeMin;
        else if(mValue>mRangeMax)
            mValue = mRangeMax;

        boolean bHer = rc.width()>=rc.height();
        if(mThumbLength>(bHer?rc.height()*2:rc.width()*2))
            mThumbLength = bHer?rc.height()*2:rc.width()*2;
        double trackX = (mValue-mRangeMin)*1.0*((bHer?rc.width():rc.height())-mThumbLength)/(mRangeMax-mRangeMin);
        Rect thumbRect = null;
        if(mThumbStyle==0) {
            if (bHer)
                thumbRect = new Rect(rc.left + (int) trackX, rc.top + rc.height() / 8, rc.left + (int) trackX + mThumbLength, rc.top + rc.height() / 8 + rc.height() * 3 / 4);
            else
                thumbRect = new Rect(rc.left + rc.width() / 8, rc.bottom - (int) trackX - mThumbLength, rc.left + rc.width() / 8 + rc.width() * 3 / 4, rc.bottom - (int) trackX);
        }
        else
        {
            if (bHer)
                thumbRect = new Rect(rc.left + (int) trackX, rc.top + rc.height() / 2 - mThumbLength/2, rc.left + (int) trackX + mThumbLength, rc.top + rc.height() / 2 + mThumbLength/2);
            else
                thumbRect = new Rect(rc.left + rc.width() / 2 - mThumbLength/2, rc.bottom - (int) trackX - mThumbLength, rc.left + rc.width() / 2 + mThumbLength/2, rc.bottom - (int) trackX);
        }
        Rect barRect = new Rect(rc);
        if(mSlotStyle == 0)
            barRect.inset(bHer?0:barRect.width()/3,bHer?barRect.height()/3:0);
        else
        {
            barRect.inset(bHer?0:(barRect.width()/2-2),bHer?(barRect.height()/2-2):0);
        }
        Rect elapsedRect = new Rect(barRect);
        if(bHer)
        {
            elapsedRect.right = elapsedRect.left + thumbRect.left - rc.left + mThumbLength/2;
        }
        else {
            elapsedRect.top = thumbRect.bottom - mThumbLength / 2;
        }

        WSUtil.gradientFill(dc,barRect,colorSlotBorder,colorSlotBackground,!bHer?2:3);
        WSUtil.gradientFill(dc,elapsedRect,colorSlotBorder,colorSlotForeground,!bHer?2:3);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colorSlotBorder);
        paint.setStrokeWidth(1);

        //draw thumb

        if(mThumbStyle == 0) {
            WSUtil.gradientFill(dc, thumbRect, /*darkColor*/colorThumbBorder, colorThumbBackground, !bHer ? 2 : 3);
        }
        else
        {
            WSUtil.gradientFillCircle(dc,thumbRect,colorThumbBackground,colorThumbBorder);
        }
    }

    @Override
    public void updateValueFromCV(CChannelValue cv) {

        if(mStyle == 0)
        {
            mValue = CChannelValue.valueToPercent(cv.mValue);
        }
        else if(mStyle == 1){
            if(mTempType == 1)
                mValue = cv.mSetTemp;
            else
                mValue = cv.mTemp;
        }
        else if(mStyle < 3)
        {
            mValue = cv.mValue;
        }
        else if(mStyle == 3)
        {
            if(cv.mV<0)
                mValue = 0;
            else
                mValue = cv.mV;
        }
        else if(mStyle == 4)
        {
            if(cv.mA<0)
                mValue = 0;
            else
                mValue = cv.mA;
        }
        else if(mStyle == 5)
        {
            if(cv.mV<0 || cv.mA<0)
                mValue = 0;
            else
                mValue = cv.mV*cv.mA;
        }
        else if(mStyle == 6)
        {
            if(cv.mValue<0)
                mValue = 0;
            else
                mValue = cv.mValue;
        }

        updateUI();

    }

    private String S_FUNCTION1 = "Function1";
    private String S_FBSTYLE = "FBStyle";
    private String S_BUS = "Bus";
    private String S_AREA = "Area";
    private String S_CHANNEL = "Channel";
    private String S_SLOT = "Slot";
    private String S_STYLE	= "Style";
    private String S_MAX = "Max";
    private String S_MIN = "Min";
    private String S_ORIENT = "Orient";
    private String S_THUMB	= "Thumb";
    private String S_BAR = "Bar";
    private String S_TICK	= "Tick";
    private String S_TEMPTYPE = "TempType";
    @Override
    public void loadXML(Element xmlData) {
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        mRangeMax = Integer.parseInt(xmlData.getAttribute(S_MAX));
        mRangeMin = Integer.parseInt(xmlData.getAttribute(S_MIN));
        String[] strings = xmlData.getAttribute(S_SLOT).split(",");
        colorSlotBackground = WSUtil.colorWinToAD(Integer.parseInt(strings[0]));
        colorSlotBorder = WSUtil.colorWinToAD(Integer.parseInt(strings[1]));
        colorSlotForeground = WSUtil.colorWinToAD(Integer.parseInt(strings[2]));
        if(strings.length>3)
            mSlotStyle = Integer.parseInt(strings[3]);
        strings = xmlData.getAttribute(S_THUMB).split(",");
        mThumbLength = Integer.parseInt(strings[0]);
        colorThumbBackground = WSUtil.colorWinToAD(Integer.parseInt(strings[1]));
        colorThumbBorder = WSUtil.colorWinToAD(Integer.parseInt(strings[2]));
        if(strings.length>3)
            mThumbStyle = Integer.parseInt(strings[3]);
        if(xmlData.getAttribute(S_FUNCTION1)!=null)
            mFun = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION1));
        mBus = Integer.parseInt(xmlData.getAttribute(S_BUS));
        mArea = Integer.parseInt(xmlData.getAttribute(S_AREA));
        mCh = Integer.parseInt(xmlData.getAttribute(S_CHANNEL));
        mStyle = Integer.parseInt(xmlData.getAttribute(S_FBSTYLE));
        if(xmlData.getAttribute(S_TEMPTYPE) != null && xmlData.getAttribute(S_TEMPTYPE).length()>0)
            mTempType = Integer.parseInt(xmlData.getAttribute(S_TEMPTYPE));

        mProject.addChannelValue(mBus,mArea,mCh).editUI(this);
    }

    private void onClick(){
        if(mFun.mBus>0 && mFun.codeData != null)
        {
            if(mFun.codeData[1] == 0x2)
            {
                mFun.codeData[6] = CChannelValue.percentToValue(mValue);
                mFun.codeData[7] = CBase.calcCheck(mFun.codeData, 7);
                mProject.uiSend(mFun);
            }
            else if(mFun.codeData[1] == 0x28 && mFun.codeData[4] == 0)
            {
                mFun.codeData[5] = (byte)mValue;
                mFun.codeData[7] = CBase.calcCheck(mFun.codeData,7);
                mProject.uiSend(mFun);
            }
        }
    }
}
