package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIFeedback extends CUIBase {

    int     mStyle;
    int     mBus;
    int     mArea;
    int     mCh;
    int     mUnit;
    int     mAlign;
    int     colorText;
    Font    mFont;
    CFunction   mFun;
    int     mPage = 0;
    int     m_nTempType;

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
        String text = getText();
        Paint paint = new Paint();
        int valign = 0,align = 0;
        switch (mAlign){
            case 0:
                break;
            case 1:
                align = 1;
                break;
            case 2:
                align = 2;
                break;
            case 3:
                valign = 1;
                break;
            case 4:
                valign = 1;
                align = 1;
                break;
            case 5:
                valign = 1;
                align = 2;
                break;
            case 6:
                valign = 2;
                break;
            case 7:
                valign = 2;
                align = 1;
                break;
            case 8:
                valign = 2;
                align = 2;
                break;
        }

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAntiAlias(true);

        paint.setColor(colorText);
        mFont.setFontToPaint(paint);

        WSUtil.drawText(dc, paint, getRect(), text, valign, align);
    }

    @Override
    public void updateValueFromCV(CChannelValue cv) {
        updateUI();
    }
    static String S_STYLE	= "Style";
    static String S_BUS = "Bus";
    static String S_AREA	 = "Area";
    static String S_CHANNEL = "Channel";
    static String S_UNIT = "Unit";

    static String S_ALIGN = "Align";
    static String S_COLOR = "Color";
    static String S_FONT = "font";
    static String S_LINKPAGE = "LinkPage";
    static String S_FUNCTION = "Function";
    static String S_TEMPTYPE = "TempType";

    @Override
    public void loadXML(Element xmlData) {
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        mAlign = Integer.parseInt(xmlData.getAttribute(S_ALIGN));
        colorText = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_COLOR)));
        mFont = Font.fontFromString(xmlData.getAttribute(S_FONT));
        if(xmlData.getAttribute(S_LINKPAGE) != null && xmlData.getAttribute(S_LINKPAGE).length()>0)
            mPage = Integer.parseInt(xmlData.getAttribute(S_LINKPAGE));
        mFun = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION));
        mBus = Integer.parseInt(xmlData.getAttribute(S_BUS));
        mArea = Integer.parseInt(xmlData.getAttribute(S_AREA));
        mCh = Integer.parseInt(xmlData.getAttribute(S_CHANNEL));
        mUnit = Integer.parseInt(xmlData.getAttribute(S_UNIT));
        mStyle =  Integer.parseInt(xmlData.getAttribute(S_STYLE));
        mProject.addChannelValue(mBus,mArea,mCh).editUI(this);
        if(xmlData.getAttribute(S_TEMPTYPE) != null && xmlData.getAttribute(S_TEMPTYPE).length()>0)
            m_nTempType = Integer.parseInt(xmlData.getAttribute(S_TEMPTYPE));
    }

    void onClick()
    {
        mProject.uiSend(mFun);

        if(mPage>0)
            mProject.setActivePage(mPage);
    }

    String getText(){
        if(mBus == 0 || mArea <=0 || mCh<=0)
            return "0%";
        CChannelValue cv = mProject.getChannelValue(mBus,mArea,mCh);
        if(cv == null)
            return "0%";

        String ret = null;
        switch (mStyle)
        {
            case 0:
                return String.format("%d%%",CChannelValue.valueToPercent(cv.mValue) );
            case 1: {
                int nValue = cv.mTemp;
                if (m_nTempType != 0)
                    nValue = cv.mSetTemp;
                if (mUnit == 0)
                    return String.format("%d ℃", nValue);
                else
                    return String.format("%d ℉", nValue);
            }
            case 2:
                return String.format("%d%%",cv.mValue);
            case 3:
                if(cv.mV<0)
                    return "NA";
                else
                    return String.format("%d V",cv.mV);
            case 4:
                if(cv.mA<0)
                    return "NA";
                else
                    return String.format("%d A",cv.mA);
            case 5:
                if(cv.mA<0 || cv.mV<0)
                    return "NA";
                else
                    return String.format("%.2f KVA",cv.mA*cv.mV/1000.);
            case 6:
                if(cv.mValue<0)
                    return "NA";
                else
                    return String.format("%d Lux",cv.mValue);
        }

        return "0%";
    }
}
