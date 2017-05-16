package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIShape extends CUIBase {
    int     mShapeType;
    int     mBorderWidth;
    boolean         mBorderIndication;
    int     colorBorderActive;
    int     mBorderStyleActive;
    int     colorBorderInactive;
    int     mBorderStyleInactive;
    int     mBorderparam;
    boolean mBackgroundIndication;
    int     colorBackgroundActive;
    int     mBackgroundStyleActive;
    int     colorBackgroundInactive;
    int     mBackgroundStyleInactive;
    Vector<Point> arrayPoints = new Vector<Point>();;
    int     mPage = 0;
    CFunction[]   mFun;
    boolean mLock = false;
    boolean mCheck = false;
    boolean mActive = false;

    Path    mPath;

    public int getBorderWidth(){
        return mBorderWidth;
    }

    @Override
    public void onShow() {
        if(!mLock && mActive)
            mActive = false;
    }

    private boolean beingMsg = false;
    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKDOWN)
        {
            if(!pointInRect((int)param1,(int)param2))
                return false;
            if(mLock && mCheck)
                return true;
            mProject.clickAudio();
            mActive = false;
            beingMsg = true;
            onClick();
            mActive = true;

            beingMsg = false;

            updateUI();
            return true;
        }
        else if(message == MESSAGE_CLICKUP && mActive)
        {
          //  if(!pointInRect((int)param1,(int)param2))
         //       return false;
            if(mLock && !mCheck) {
                mCheck = true;
                return true;
            }
            if(mLock)
                mProject.clickAudio();
            onClick();
            mActive = false;
            mCheck = false;
            updateUI();

            return true;
        }

        return false;

    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        drawBackground(dc);
        drawBorder(dc);
    }

    static String S_SHAPE = "Shape";
    static String S_BORDERWIDTH = "BorderWidth";
    static String S_BORDERINDICATION = "BorderIndication";
    static String S_BORDERACTIVECOLOR = "BorderActiveColor";
    static String S_BORDERACTIVESTYLE = "BorderActiveStyle";
    static String S_BORDERINACTIVECOLOR = "BorderInactiveColor";
    static String S_BORDERINACTIVESTYLE = "BorderInactiveStyle";
    static String S_BACKGROUNDINDICATION = "BackgroundIndication";
    static String S_BACKGROUNDACTIVECOLOR = "BackgroundActiveColor";
    static String S_BACKGROUNDACTIVESTYLE = "BackgroundActiveStyle";
    static String S_BACKGROUNDINACTIVECOLOR = "BackgroundInactiveColor";
    static String S_BACKGROUNDINACTIVESTYLE = "BackgroundInactiveStyle";
    static String S_BORDERPARAM = "BorderParam";
    static String S_POINT	= "Point";
    static String S_LINKPAGE = "LinkPage";
    static String S_FUNCTION1 = "Function1";
    static String S_FUNCTION2 = "Function2";
    static String S_LOCK = "Lock";

    @Override
    public void loadXML(Element xmlData) {
        mShapeType = Integer.parseInt(xmlData.getAttribute(S_SHAPE));
        mBorderWidth = Integer.parseInt(xmlData.getAttribute(S_BORDERWIDTH));
        mBorderIndication = Integer.parseInt(xmlData.getAttribute(S_BORDERINDICATION)) == 1;
        colorBorderActive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BORDERACTIVECOLOR)));
        mBorderStyleActive = Integer.parseInt(xmlData.getAttribute(S_BORDERACTIVESTYLE));
        colorBorderInactive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BORDERINACTIVECOLOR)));
        mBorderStyleInactive = Integer.parseInt(xmlData.getAttribute(S_BORDERINACTIVESTYLE));
        mBackgroundIndication = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINDICATION)) == 1;
        colorBackgroundActive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDACTIVECOLOR)));
        mBackgroundStyleActive = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDACTIVESTYLE));
        colorBackgroundInactive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINACTIVECOLOR)));
        mBackgroundStyleInactive = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINACTIVESTYLE));
        mBorderparam = Integer.parseInt(xmlData.getAttribute(S_BORDERPARAM));

        if(xmlData.getAttribute(S_LINKPAGE) != null && xmlData.getAttribute(S_LINKPAGE).length()>0)
            mPage = Integer.parseInt(xmlData.getAttribute(S_LINKPAGE));
        mFun = new CFunction[2];
        mFun[0] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION1));
        mFun[1] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION2));
        mLock = Integer.parseInt(xmlData.getAttribute(S_LOCK)) == 1;
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        if(mShapeType == SHAPE_ARBITRARY || mShapeType == SHAPE_LINE)
        {
            NodeList nodes = xmlData.getElementsByTagName(S_POINT);
            for (int i=0;i<nodes.getLength();i++)
            {
                Element item = (Element)nodes.item(i);

                String stringPoint = item.getAttribute(S_POINT);
                Point point = new Point();
                String[] strings = stringPoint.split("X");
                if(strings == null || strings.length!=2)
                    continue;;
                point.x = Integer.parseInt(strings[0]);
                point.y = Integer.parseInt(strings[1]);

                arrayPoints.add(point);
            }
        }
    }

    void onClick()
    {
        if(mActive && mFun != null && mFun.length == 2)
            mProject.uiSend(mFun[1]);
        else if(!mActive && mFun!=null && mFun.length>0)
            mProject.uiSend(mFun[0]);
        if(mPage>0 && mActive)
            mProject.setActivePage(mPage);
    }

    boolean pointInRect(int x,int y)
    {
        if(mShapeType == SHAPE_LINE)
        {
            if(arrayPoints.size()!=2)
                return false;
            return isPointAtLine(new Point(x,y),arrayPoints.elementAt(0),arrayPoints.elementAt(1),1.5);
        }
        if(mPath == null)
            mPath = createPath(getRect());
        if(mPath == null)
            return false;
        Region ui = new Region();
        if(mShapeType != SHAPE_LINE)
            ui.setPath(mPath,new Region(getRect()));
        else
        {
            Path path = new Path();
            path.moveTo(arrayPoints.elementAt(0).x,arrayPoints.elementAt(0).y);
            path.lineTo(arrayPoints.elementAt(0).x,arrayPoints.elementAt(0).y+1);
            path.lineTo(arrayPoints.elementAt(1).x,arrayPoints.elementAt(1).y+1);
            path.lineTo(arrayPoints.elementAt(1).x,arrayPoints.elementAt(1).y);
            path.close();
            Rect rc = new Rect(getRect());
            rc.inset(-1,-1);
            ui.setPath(mPath,new Region(rc));
        }
        Path tmp = new Path();
        Region ptRegion = new Region(x-1,y-1,x,y);
        return (!ui.quickReject(ptRegion) && ui.op(ptRegion, Region.Op.INTERSECT));
    }

    double distanceBetweenPointAt(Point pointA,Point pointB)
    {
        return Math.sqrt(Math.pow((pointA.x-pointB.x),2) + Math.pow((pointA.y-pointB.y),2));
    }

    boolean isPointAtLine(Point origin,Point linePointA,Point linePointB,double margin)
    {
        double distanceAP = distanceBetweenPointAt(origin,linePointA);
        double distanceBP = distanceBetweenPointAt(origin,linePointB);
        double distanceAB = distanceBetweenPointAt(linePointA,linePointB);

        return (Math.abs(distanceAB-distanceAP-distanceBP)<margin);
    }

    Path createPath(Rect rc){
        Path path = new Path();
        if(mShapeType == SHAPE_LINE)
        {
            if(arrayPoints.size()!=2)
                return null;
            path.moveTo(arrayPoints.elementAt(0).x,arrayPoints.elementAt(0).y);
            path.lineTo(arrayPoints.elementAt(1).x,arrayPoints.elementAt(1).y);
        }
        else if(mShapeType == SHAPE_ELLIPSE)
        {
            path.addCircle(rc.centerX(),rc.centerY(),rc.width()>=rc.height()?rc.height()/2:rc.width()/2,Path.Direction.CW);
        }
        else if(mShapeType == SHAPE_CAPSULE)
        {
            float f = rc.width()>rc.height()?rc.height()/2:rc.width()/2;
            RectF rf = new RectF(rc);
            path.addRoundRect(rf,f,f, Path.Direction.CW);
        }
        else if(mShapeType == SHAPE_RECT)
        {
            path.addRect(rc.left,rc.top,rc.right,rc.bottom,Path.Direction.CW);
        }
        else if(mShapeType == SHAPE_ROUNDRECT)
        {
            path.addRoundRect(new RectF(rc.left,rc.top,rc.right,rc.bottom),
                    new float[]{mBorderparam,mBorderparam,mBorderparam,mBorderparam,mBorderparam,mBorderparam,mBorderparam,mBorderparam}
                    ,Path.Direction.CW);
        }
        else if(mShapeType ==  SHAPE_POLYGON)
        {
            if(mBorderparam<3)
                return null;
            double dbRadius = rc.width()>=rc.height()?rc.height()/2:rc.width()/2;
            double angle = 2*PI/mBorderparam;
            for (int i=0;i<mBorderparam;i++)
            {
                if(i==0)
                    path.moveTo(rc.centerX()+(int)(dbRadius*Math.cos(i*angle-(mBorderparam%2==0?0:PI/2))),
                            rc.centerY()+(int)(dbRadius*Math.sin(i * angle - (mBorderparam % 2 == 0 ? 0 : PI / 2))));
                else
                    path.lineTo(rc.centerX()+(int)(dbRadius*Math.cos(i*angle-(mBorderparam%2==0?0:PI/2))),
                            rc.centerY()+(int)(dbRadius*Math.sin(i*angle-(mBorderparam%2==0?0:PI/2))));
            }
            path.close();
        }
        else if(mShapeType == SHAPE_TRIANGLE_RIGHT)
        {
            path.moveTo(rc.left,rc.top);
            path.lineTo(rc.left,rc.bottom);
            path.lineTo(rc.right,rc.centerY());
            path.close();
        }
        else if(mShapeType == SHAPE_TRIANGLE_LEFT)
        {
            path.moveTo(rc.left,rc.centerY());
            path.lineTo(rc.right,rc.bottom);
            path.lineTo(rc.right,rc.top);
            path.close();
        }
        else if (mShapeType == SHAPE_TRIANGLE_UP)
        {
            path.moveTo(rc.left,rc.bottom);
            path.lineTo(rc.right,rc.bottom);
            path.lineTo(rc.centerX(),rc.top);
            path.close();
        }
        else if(mShapeType == SHAPE_TRIANGLE_DOWN)
        {
            path.moveTo(rc.left,rc.top);
            path.lineTo(rc.centerX(),rc.bottom);
            path.lineTo(rc.right,rc.top);
            path.close();
        }
        else if(mShapeType == SHAPE_POINTER_LEFT)
        {
            Rect rect = new Rect(rc);
            if(rc.width()<rc.height())
                rect.inset(0,(rect.height()-rect.width())/2);
            path.moveTo(rect.left,rect.centerY());
            path.lineTo(rect.left+rect.height()/2,rect.bottom);
            path.lineTo(rect.right,rect.bottom);
            path.lineTo(rect.right,rect.top);
            path.lineTo(rect.left+rect.height()/2,rect.top);
            path.close();;
        }
        else if(mShapeType == SHAPE_POINTER_LEFTRIGHT)
        {
            Rect rect = new Rect(rc);
            if(rc.width()<rc.height())
                rect.inset(0,(rect.height()-rect.width())/2);
            path.moveTo(rect.left,rect.centerY());
            path.lineTo(rect.left+rect.height()/2,rect.bottom);
            path.lineTo(rect.right-rect.height()/2,rect.bottom);
            path.lineTo(rect.right,rect.centerY());
            path.lineTo(rect.right-rect.height()/2,rect.top);
            path.lineTo(rect.left+rect.height()/2,rect.top);
            path.close();
        }
        else if(mShapeType == SHAPE_POINTER_RIGHT)
        {
            Rect rect = new Rect(rc);
            if(rc.width()<rc.height())
                rect.inset(0,(rect.height()-rect.width())/2);
            path.moveTo(rect.left,rect.top);
            path.lineTo(rect.left,rect.bottom);
            path.lineTo(rect.right-rect.height()/2,rect.bottom);
            path.lineTo(rect.right,rect.centerY());
            path.lineTo(rect.right-rect.height()/2,rect.top);
            path.close();
        }
        else if(mShapeType == SHAPE_POINTER_UP)
        {
            Rect rect = new Rect(rc);
            if(rc.width()>rc.height())
                rect.inset((rect.width()-rect.height())/2,0);
            path.moveTo(rect.centerX(),rect.top);
            path.lineTo(rect.left,rect.top+rect.width()/2);
            path.lineTo(rect.left,rect.bottom);
            path.lineTo(rect.right,rect.bottom);
            path.lineTo(rect.right,rect.top+rect.width()/2);
            path.close();;
        }
        else if(mShapeType == SHAPE_POINTER_UPDOWN)
        {
            Rect rect = new Rect(rc);
            if(rc.width()>rc.height())
                rect.inset((rect.width()-rect.height())/2,0);
            path.moveTo(rect.centerX(), rect.top);
            path.lineTo(rect.left, rect.top + rect.width() / 2);
            path.lineTo(rect.left,rect.bottom-rect.width()/2);
            path.lineTo(rect.centerX(),rect.bottom);
            path.lineTo(rect.right,rect.bottom-rect.width()/2);
            path.lineTo(rect.right,rect.top+rect.width()/2);
            path.close();
        }
        else if(mShapeType == SHAPE_POINTER_DOWN)
        {
            Rect rect = new Rect(rc);
            if(rc.width()>rc.height())
                rect.inset((rect.width()-rect.height())/2,0);
            path.moveTo(rect.left,rect.top);
            path.lineTo(rect.left,rect.bottom-rect.width()/2);
            path.lineTo(rect.centerX(),rect.bottom);
            path.lineTo(rect.right,rect.bottom-rect.width()/2);
            path.lineTo(rect.right,rect.top);
            path.close();
        }
        else if(mShapeType == SHAPE_STAR)
        {
            if(mBorderparam < 4)
                return null;

            int dbRadius = rc.width()>rc.height()?rc.height()/2:rc.width()/2;
            double angle = 2*PI/mBorderparam;
            for (int i=0;i<mBorderparam;i++)
            {
                if(i==0)
                    path.moveTo(rc.centerX()+(int)(dbRadius*Math.cos(i*angle-(mBorderparam%2==0?0:PI/2))),
                            rc.centerY()+(int)(dbRadius*Math.sin(i * angle - (mBorderparam % 2 == 0 ? 0 : PI / 2))));
                else
                    path.lineTo(rc.centerX()+(int)(dbRadius*Math.cos(i*angle-(mBorderparam%2==0?0:PI/2))),
                            rc.centerY()+(int)(dbRadius*Math.sin(i*angle-(mBorderparam%2==0?0:PI/2))));
                path.lineTo(rc.centerX()+(int)(dbRadius/2*Math.cos(angle/2+i*angle-(mBorderparam%2==0?0:PI/2))),
                        rc.centerY()+(int)(dbRadius/2*Math.sin(angle/2+i*angle-(mBorderparam%2==0?0:PI/2))));
            }
            path.close();
        }
        else if(mShapeType == SHAPE_ARBITRARY)
        {
            path.moveTo(arrayPoints.elementAt(0).x,arrayPoints.elementAt(0).y);
            for (int i=1;i<arrayPoints.size();i++)
                path.lineTo(arrayPoints.elementAt(i).x,arrayPoints.elementAt(i).y);
            path.close();;
        }
        else
            return null;

        return path;
    }

    void setPaintLine(Paint paint,int style)
    {
        int type = style;
        if(type == 1)
        {
            DashPathEffect pe = new DashPathEffect(new float[]{5,2},1);
            paint.setPathEffect(pe);
        }
        else if(type == 2)
        {
            DashPathEffect pe = new DashPathEffect(new float[]{20,2,1,2,20,2,1,2},1);
            paint.setPathEffect(pe);
        }
        else
            paint.setPathEffect(null);
    }

    void drawBackground(Canvas dc){
        if(mShapeType == SHAPE_LINE)
            return;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if(mBackgroundIndication)
        {
            if(mActive)
                paint.setColor(colorBackgroundActive);
            else
                paint.setColor(colorBackgroundInactive);
        }
        else
            paint.setColor(colorBackgroundActive);
        paint.setStyle(Paint.Style.FILL);

        if(mPath == null && (mPath=createPath(getRect())) == null)
            return;
        dc.drawPath(mPath,paint);
    }

    void drawBorder(Canvas dc){
        int nStyle = mBorderStyleActive;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        if(mBorderIndication && !mActive) {
            nStyle = mBorderStyleInactive;
            paint.setColor(colorBorderInactive);
        }
        else
            paint.setColor(colorBorderActive);

        if(nStyle == 0)//no border
            return;
        else if(nStyle == 1 || nStyle == 2 || nStyle == 3)
        {
            if(mPath == null && (mPath=createPath(getRect())) == null)
                return;
            setPaintLine(paint,nStyle);
            dc.drawPath(mPath,paint);
        }
        else if(nStyle == 4)//solid
        {
            if(mPath == null && (mPath=createPath(getRect())) == null)
                return;
            paint.setStrokeWidth(mBorderWidth);
            dc.drawPath(mPath,paint);
        }
        else if(nStyle == 5)
        {
            if(mPath == null && (mPath=createPath(getRect())) == null)
                return;
            paint.setStrokeWidth(1);
            Rect rectInner = new Rect(getRect());
            rectInner.inset(mBorderWidth,mBorderWidth);
            Path pathInner = createPath(rectInner);
            if(pathInner == null)
                return;
            dc.drawPath(mPath,paint);
            dc.drawPath(pathInner,paint);
        }
    }

    public void setCheck(Boolean check)
    {
        if(!mLock)
            return;
        mCheck = check;
        mActive = check;
        updateUI();
    }

    @Override
    public void updateDataFromBus(int bus,byte[] buf) {
        if(!mLock)
            return;
        if(!beingMsg && mFun[0] != null && mFun[0].codeData != null && mFun[0].mFunction == 0  && bus == mFun[0].mBus && mFun[0].codeData.length == buf.length)
        {
            if(Arrays.equals(buf,mFun[0].codeData))
            {
                setCheck(true);
                return;
            }
        }
        if(mFun[1] != null && mFun[1].codeData != null && mFun[1].mFunction == 0  && bus == mFun[0].mBus && mFun[1].codeData.length == buf.length)
        {
            if(Arrays.equals(buf,mFun[1].codeData))
            {
                setCheck(false);
                return;
            }
        }

    }
}
