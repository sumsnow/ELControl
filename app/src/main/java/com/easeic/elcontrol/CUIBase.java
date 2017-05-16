package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.Rect;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/24.
 */
public class CUIBase extends CBase{

    public static final String S_CLASS = "Class";
    public static final String S_UI = "UI";
    public static final String S_RECT = "Rect";
    public static final double PI = 3.1415926;

    //shape

    public static final int SHAPE_RECT = 1;
    public static final int SHAPE_ROUNDRECT = 2;
    public static final int SHAPE_CAPSULE = 3;
    public static final int SHAPE_POLYGON = 4;
    public static final int SHAPE_ELLIPSE = 5;
    public static final int SHAPE_TRIANGLE_RIGHT = 6;
    public static final int SHAPE_TRIANGLE_LEFT = 7;
    public static final int SHAPE_TRIANGLE_UP = 8;
    public static final int SHAPE_TRIANGLE_DOWN = 9;
    public static final int SHAPE_POINTER_LEFT = 10;
    public static final int SHAPE_POINTER_RIGHT = 11;
    public static final int SHAPE_POINTER_LEFTRIGHT = 12;
    public static final int SHAPE_POINTER_UP = 13;
    public static final int SHAPE_POINTER_DOWN = 14;;
    public static final int SHAPE_POINTER_UPDOWN = 15;
    public static final int SHAPE_STAR = 16;
    public static final int SHAPE_LINE = 17;
    public static final int SHAPE_ARBITRARY = 18;

    //Message
    public static final int MESSAGE_CLICKDOWN = 2000;
    public static final int MESSAGE_CLICKUP = 2001;
    public static final int MESSAGE_TIMER = 2002;
    public static final int MESSAGE_UPDATE = 2003;
    public static final int MESSAGE_MOUSEMOVE = 2004;

    //UI TYPE
    public static final int UICLASS_PAGE = 2;
    public static final int UICLASS_SHAPE = 1;
    public static final int UICLASS_BUTTON = 3;
    public static final int UICLASS_SLIDER = 4;
    public static final int UICLASS_TEXT = 5;
    public static final int UICLASS_IMAGE = 6;
    public static final int UICLASS_FEEDBACK = 7;
    public static final int UICLASS_LOAD = 8;
    public static final int UICLASS_CLOCK = 9;
    public static final int UICLASS_SUBPAGE = 10;

    //properties
    public Rect mRect;
    public CUIBase mOwner;

    //method
    public Rect getRect(){
        return new Rect(mRect);
    }

    public Boolean onMessage(int message,float param1,float param2,float param3)
    {
        return false;
    }

    public void onDraw(Canvas dc,Rect rect)
    {

    }

    public void onShow(){

    }

    public void updateUI()
    {
        if(mProject != null && mProject.mView != null)
        {
            if(this instanceof CUIPage){
                //mProject.mView.postInvalidate();
                CUIPage page = (CUIPage)this;
                if(mProject.mView.mAnimating || !ELConfigure.mCanAnimation || !mProject.mView.needAnimation())
                    mProject.mView.postInvalidate();
                return;
            }
            Rect rc = getRect();

            if(rc == null)
                return;

            if(this instanceof CUIButton)
            {
                CUIButton btn = (CUIButton)this;
                if(btn.getBorderWidth()>1)
                {
                    rc.inset(-btn.getBorderWidth()/2-1,-btn.getBorderWidth()/2-1);
                }
            }
            else if(this instanceof CUILoad)
            {
                CUILoad btn = (CUILoad) this;
                if(btn.getBorderWidth()>1)
                {
                    rc.inset(-btn.getBorderWidth()/2-1,-btn.getBorderWidth()/2-1);
                }
            }
            else if(this instanceof CUIShape)
            {
                CUIShape btn = (CUIShape) this;
                if(btn.getBorderWidth()>1)
                {
                    rc.inset(-btn.getBorderWidth()/2-1,-btn.getBorderWidth()/2-1);
                }
            }

            if(mOwner != null && mOwner instanceof CSubPage)
            {
                CSubPage sub = (CSubPage)mOwner;

                rc = sub.LogToPhy(rc);
            }

            double dbScale = mProject.mView.mScale;


          //  mProject.mView.invalidate((int)(rc.left*dbScale+mProject.mView.mLeft)-1, (int)(rc.top*dbScale+mProject.mView.mTop)-1, (int)(rc.right*dbScale+mProject.mView.mLeft)+1, (int)(rc.bottom*dbScale+mProject.mView.mTop)+1);
         //   mProject.mView.postInvalidate((int)(rc.left*dbScale+mProject.mView.mLeft)-1, (int)(rc.top*dbScale+mProject.mView.mTop)-1, (int)(rc.right*dbScale+mProject.mView.mLeft)+2, (int)(rc.bottom*dbScale+mProject.mView.mTop)+2);
            mProject.mView.postInvalidate();

        }
    }

    public void updateValueFromCV(CChannelValue cv){

    }

    public void loadXML(Element xmlData){

    }

    public Rect rectFromString(String stringRect){
        String[] strings = stringRect.split("\\;");
        if(strings == null || strings.length != 2)
            return null;
        String[] leftTopStrings = strings[0].split("X");
        String[] rightBottomStrings = strings[1].split("X");
        if(leftTopStrings == null || leftTopStrings.length!=2
                || rightBottomStrings==null || rightBottomStrings.length != 2)
            return null;
        Rect ret = new Rect();
        ret.left = Integer.parseInt(leftTopStrings[0]);
        ret.top = Integer.parseInt(leftTopStrings[1]);
        ret.right = Integer.parseInt(rightBottomStrings[0]);
        ret.bottom = Integer.parseInt(rightBottomStrings[1]);

        return ret;
    }

    public void updateDataFromBus(int bus,byte[] buf){
    }
}
