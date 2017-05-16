package com.easeic.elcontrol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Vector;

/**
 * Created by sam on 2016/3/25.
 */
public class CUILoad extends CUIBase {  int     mShapeType;
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
    CFunction  mFun;
    boolean mLock = false;
    boolean mCheck = false;
    boolean mActive = false;

    int mValueType = 0;
    //title
    int     mAlign;
    boolean mTextIndicator;
    String  textActive;
    int     colorTextActive;
    Font    fontActive;
    String  textInactive;
    int     colorTextInactive;
    Font    fontInactive;

    //image
    boolean mShowImage = false;
    int     imageAlign = 0;
    int     imageIndex = -1;

    Bitmap imageActive;
    Bitmap  imageInactive;

    int     mBus = 0;
    int     mArea = 0;

    @Override
    public void updateValueFromCV(CChannelValue cv) {
        updateUI();
    }

    int     mCh = 0;

    Path mPath;

    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKDOWN)
        {
            if(!pointInRect((int)param1,(int)param2))
                return false;

            mProject.clickAudio();
                onClick();
            return true;
        }
        return false;

    }

    public int getBorderWidth(){
        return mBorderWidth;
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        mActive = isActive();
        drawBackground(dc);
        drawBorder(dc);
        drawImage(dc);
        drawText(dc);
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
    static String S_FUNCTION3	= "Function3";
    static String S_FUNCTION4	= "Function4";
    static String S_LOCK = "Lock";
    static String S_GROUP	= "Group";
    static String S_B4KEY = "B4Key";

    static String  S_IMAGEACTIVE = "ImageActive";
    static String  S_IMAGEINACTIVE	= "ImageInactive";
    static String  S_SHOWIMAGE	= "ShowImage";
    static String  S_IMAGEINDEX = "ImageIndex";
    static String  S_IMAGEALIGN = "ImageAlign";

    static String  S_TEXTINDICATOR	= "TextIndicator";
    static String  S_TEXTALIGN	= "TextAlign";
    static String  S_TEXTACTIVE = "TextActive";
    static String  S_COLORACTIVE = "ColorActive";
    static String  S_FONTACTIVE = "FontActive";
    static String  S_TEXTINACTIVE	= "TextInactive";
    static String  S_COLORINACTIVE =	"ColorInactive";
    static String  S_FONTINACTIVE	= "FontInactive";
    static String S_BUS = "Bus";
    static String S_AREA	 = "Area";
    static String S_CHANNEL = "Channel";
    static String S_VALUETYPE = "ValueType";

    static int  S_ImageIndex[] = {R.drawable._buttonimage0,R.drawable._buttonimage1,R.drawable._buttonimage2,R.drawable._buttonimage3
            ,R.drawable._buttonimage4,R.drawable._buttonimage5,R.drawable._buttonimage6,R.drawable._buttonimage7,R.drawable._buttonimage8
            ,R.drawable._buttonimage9,R.drawable._buttonimage10,R.drawable._buttonimage11,R.drawable._buttonimage12,R.drawable._buttonimage13};

    @Override
    public void loadXML(Element xmlData) {
        mShapeType = Integer.parseInt(xmlData.getAttribute(S_SHAPE));
        mBorderWidth = Integer.parseInt(xmlData.getAttribute(S_BORDERWIDTH));
        mBorderIndication = Integer.parseInt(xmlData.getAttribute(S_BORDERINDICATION)) == 1;
        colorBorderActive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BORDERACTIVECOLOR)) );
        mBorderStyleActive = Integer.parseInt(xmlData.getAttribute(S_BORDERACTIVESTYLE));
        colorBorderInactive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BORDERINACTIVECOLOR)) );
        mBorderStyleInactive = Integer.parseInt(xmlData.getAttribute(S_BORDERINACTIVESTYLE));
        mBackgroundIndication = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINDICATION)) == 1;
        colorBackgroundActive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDACTIVECOLOR)) );
        mBackgroundStyleActive = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDACTIVESTYLE));
        colorBackgroundInactive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINACTIVECOLOR)));
        mBackgroundStyleInactive = Integer.parseInt(xmlData.getAttribute(S_BACKGROUNDINACTIVESTYLE));
        mBorderparam = Integer.parseInt(xmlData.getAttribute(S_BORDERPARAM));

        if(xmlData.getAttribute(S_LINKPAGE) != null && xmlData.getAttribute(S_LINKPAGE).length()>0)
            mPage = Integer.parseInt(xmlData.getAttribute(S_LINKPAGE));
        mFun = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION1));

        mRect = rectFromString(xmlData.getAttribute(S_RECT));

        //image
        mShowImage = Integer.parseInt(xmlData.getAttribute(S_SHOWIMAGE)) == 1;
        imageAlign = Integer.parseInt(xmlData.getAttribute(S_IMAGEALIGN));
        imageIndex = Integer.parseInt(xmlData.getAttribute(S_IMAGEINDEX));
        if(xmlData.getAttribute(S_IMAGEACTIVE) != null)
            imageActive = WSUtil.loadIOBitmap(mProject.getResFile(xmlData.getAttribute(S_IMAGEACTIVE)));
        if(xmlData.getAttribute(S_IMAGEINACTIVE) != null)
            imageInactive = WSUtil.loadIOBitmap(mProject.getResFile(xmlData.getAttribute(S_IMAGEINACTIVE)));
        if(imageIndex>=0 && imageIndex<14)
        {
            imageActive = BitmapFactory.decodeResource(MyApplication.getAppContext().getResources(), S_ImageIndex[imageIndex]);
            if(imageActive != null)
                imageActive = WSUtil.transparentBitmap(imageActive);
        }
        //text
        mTextIndicator = Integer.parseInt(xmlData.getAttribute(S_TEXTINDICATOR)) == 1;
        mAlign = Integer.parseInt(xmlData.getAttribute(S_TEXTALIGN));
        textActive = WSUtil.backToReturnString(xmlData.getAttribute(S_TEXTACTIVE));
        colorTextActive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_COLORACTIVE)));
        fontActive = Font.fontFromString(xmlData.getAttribute(S_FONTACTIVE));
        textInactive = WSUtil.backToReturnString(xmlData.getAttribute(S_TEXTINACTIVE));
        colorTextInactive = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_COLORINACTIVE)));
        fontInactive = Font.fontFromString(xmlData.getAttribute(S_FONTINACTIVE));

        mBus = Integer.parseInt(xmlData.getAttribute(S_BUS));
        mArea = Integer.parseInt(xmlData.getAttribute(S_AREA));
        mCh = Integer.parseInt(xmlData.getAttribute(S_CHANNEL));
        String strValueType = xmlData.getAttribute(S_VALUETYPE);
        if(strValueType != null && strValueType.length()>0)
            mValueType = Integer.parseInt(xmlData.getAttribute(S_VALUETYPE));

        if(mValueType == 0)
            mProject.addChannelValue(mBus,mArea,mCh).editUI(this);
        else{
            CTransport transport = mProject.getTransport(mBus);
            if(transport != null)
                transport.editUI(this);
        }

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
        mProject.uiSend(mFun);
        if(mPage>0 && mActive)
            mProject.setActivePage(mPage);
    }

    boolean pointInRect(int x,int y)
    {

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

    Path createPath(Rect rc){
        Path path = new Path();
        if(mShapeType == SHAPE_LINE)
        {
            if(arrayPoints.size()!=2)
                return null;
            path.moveTo(arrayPoints.elementAt(0).x,arrayPoints.elementAt(0).y);
            path.moveTo(arrayPoints.elementAt(1).x,arrayPoints.elementAt(1).y);
        }
        else if(mShapeType == SHAPE_ELLIPSE)
        {
            path.addCircle(rc.centerX(),rc.centerY(),rc.width()>=rc.height()?rc.height()/2:rc.width()/2,Path.Direction.CW);
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
                path.lineTo(rc.centerX()+(int)(dbRadius*Math.cos(angle/2+i*angle-(mBorderparam%2==0?0:PI/2))),
                        rc.centerY()+(int)(dbRadius*Math.sin(angle/2+i*angle-(mBorderparam%2==0?0:PI/2))));
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
            if(mActive||(mLock && mCheck))
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
        if(mBorderIndication && !(mActive || (mLock && mCheck))) {
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
        else if(nStyle == 6)
        {
            if(mPath == null && (mPath=createPath(getRect())) == null)
            return;

            int colorLine = colorBorderActive;
            if(mBorderIndication && !(mActive || (mLock && mCheck)))
                colorLine = colorBorderInactive;
            int factor = 10;
            int red = Color.red(colorLine),blue = Color.blue(colorLine),green = Color.green(colorLine),alpha = 0;
            Path newPath = mPath;
            for (int i=0;i<mBorderWidth;i++)
            {
                paint.setColor(Color.argb(255, red + i * factor, green + i * factor, blue + i * blue));
                dc.drawPath(newPath,paint);
                Rect tmpRect = new Rect(getRect());
                tmpRect.inset(i+1,i+1);
                newPath = createPath(tmpRect);
            }
        }
        else if(nStyle == 7)
        {
            if(mPath == null && (mPath=createPath(getRect())) == null)
                return;

            int colorLine = colorBorderActive;
            if(mBorderIndication && !(mActive || (mLock && mCheck)))
                colorLine = colorBorderInactive;
            int factor = 10;
            int red = Color.red(colorLine),blue = Color.blue(colorLine),green = Color.green(colorLine),alpha = 0;
            Path newPath = mPath;
            for (int i=0;i<mBorderWidth;i++)
            {
                paint.setColor(Color.argb(255,red-i*factor,green-i*factor,blue-i*blue));
                dc.drawPath(newPath,paint);
                Rect tmpRect = new Rect(getRect());
                tmpRect.inset(i+1,i+1);
                newPath = createPath(tmpRect);
            }
        }
    }

    void drawImage(Canvas dc)
    {
        if(!mShowImage)
            return;
        Bitmap image = imageActive;
        if(imageIndex<0 && !(mActive || (mLock && mCheck)))
            image = imageInactive;
        if(image == null)
            return;
        Rect rc = new Rect(getRect());
        rc.inset(mBorderWidth+1,mBorderWidth+1);
        double xScale = rc.width()*1.0/image.getWidth();
        double yScale = rc.height()*1.0/image.getHeight();
        xScale = xScale<yScale?xScale:yScale;
        xScale = xScale<1?xScale:1;
        int left = rc.left;
        int top = rc.top;
        switch (imageAlign)
        {
            case 0:
                break;
            case 1:
                left += (rc.width()-image.getWidth()*xScale)/2;
                break;
            case 2:
                left = rc.right - (int)(image.getWidth()*xScale);
                break;
            case 3:
                top += (rc.height()-image.getHeight()*xScale)/2;
                break;
            case 4:
                top += (rc.height()-image.getHeight()*xScale)/2;
                left += (rc.width()-image.getWidth()*xScale)/2;
                break;
            case 5:
                top += (rc.height()-image.getHeight()*xScale)/2;
                left = rc.right - (int)(image.getWidth()*xScale);
                break;
            case 6:
                top = rc.bottom - (int)(image.getHeight()*xScale);
                break;
            case 7:
                top = rc.bottom - (int)(image.getHeight()*xScale);
                left += (rc.width()-image.getWidth()*xScale)/2;
                break;
            case 8:
                top = rc.bottom - (int)(image.getHeight()*xScale);
                left = rc.right - (int)(image.getWidth()*xScale);
                break;
        }

        dc.drawBitmap(image,new Rect(0,0,image.getWidth(),image.getHeight()),new Rect(left,top,left+(int)(image.getWidth()*xScale),top+(int)(image.getHeight()*xScale)),null);
    }

    void drawText(Canvas dc)
    {
        String text = textActive;
        Font font = fontActive;
        int colorText = colorTextActive;
        if(mTextIndicator && !(mActive || (mLock && mCheck))) {
            text = textInactive;
            font = fontInactive;
            colorText = colorTextInactive;
        }
        if(text == null || text.length()<1 || font == null)
            return;

        Rect rc = new Rect(getRect());
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

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(colorText);
        font.setFontToPaint(paint);

        WSUtil.drawText(dc, paint, rc, text, valign, align);
    }

    boolean isActive(){
        if(mValueType == 1)
        {
            CTransport transport = mProject.getTransport(mBus);
            if(transport != null)
                return transport.isConnected();

            return false;
        }
        if(mBus < 1 || mArea < 1 || mCh < 1)
            return false;
        CChannelValue cv = mProject.getChannelValue(mBus,mArea,mCh);
        if(cv != null)
            return cv.mValue>0;

        return false;
    }
}

