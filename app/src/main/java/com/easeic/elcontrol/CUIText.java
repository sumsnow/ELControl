package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIText extends CUIBase {

    String mText;
    int     mAlign;
    int     colorText;
    Font    mFont;
    CFunction   mFun;
    int     mPage = 0;

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
        String text = mText;
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
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

        WSUtil.drawText(dc,paint, getRect(), text, valign, align);
    }
    static String S_TEXT = "Text";
    static String S_ALIGN = "Align";
    static String S_COLOR = "Color";
    static String S_FONT = "font";
    static String S_LINKPAGE = "LinkPage";
    static String S_FUNCTION = "Function";
    @Override
    public void loadXML(Element xmlData) {
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        mAlign = Integer.parseInt(xmlData.getAttribute(S_ALIGN));
        colorText = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_COLOR)));
        mFont = Font.fontFromString(xmlData.getAttribute(S_FONT));
        mText = WSUtil.backToReturnString(xmlData.getAttribute(S_TEXT));

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
