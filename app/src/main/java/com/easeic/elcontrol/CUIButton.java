package com.easeic.elcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Vector;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIButton extends CUIBase{
    public int mGroup = 0;
    public Boolean mEnable = true;
    public void setCheck(Boolean check)
    {
        if(!mLock)
            return;
        mCheck = check;
        mActive = check;
        updateUI();
    }
    int     mShapeType;
    int     mBorderWidth;
    boolean         mBorderIndication;

    @Override
    public void onShow() {
        if(!mLock && mActive)
            mActive = false;
    }

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
    CFunction[]   mFun = new CFunction[4];
    boolean mLock = false;
    boolean mCheck = false;
    boolean mActive = false;

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

    Bitmap  imageActive;
    Bitmap  imageInactive;

    //psw
    boolean mEnablePSW = false;
    String  stringPSW;
    boolean mEveryTime = false;
    boolean mHavePsw = false;

    Path mPath;

    boolean mAlreadyShortPress = false;
    boolean mAlreadyLongPress = false;
    long    mTick = 0;
    boolean m4Key = false;
    int m4KeyPos = 0;

    public int getBorderWidth(){
        return mBorderWidth;
    }

    private boolean beingMsg = false;
    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKDOWN)
        {
            if(!pointInRect((int)param1,(int)param2))
                return false;
            if(mLock && (mCheck || mActive))
                return true;
            if(!mEnable)
                return true;
            mProject.clickAudio();
            //check psw
            boolean bUp = false;
            if(mEnablePSW && (!mHavePsw || mEveryTime) && stringPSW != null && stringPSW.length()>0)
            {
                checkPassword();
                return true;
            }
            mTick = System.currentTimeMillis();
            mActive = false;
            beingMsg = true;
            if(!m4Key)
                onClick();
            else
                m4KeyPos = -1;
            mActive = true;
            beingMsg = false;
           // mCheck = true;
            updateUI();
            if(mLock && mGroup>0)
            {
                if(mOwner instanceof CUIPage) {
                    CUIPage pPage = (CUIPage) mOwner;
                    if (pPage != null)
                        pPage.pressGroupFromUI(this);
                }
                else{
                    CSubPage pPage = (CSubPage) mOwner;
                    if (pPage != null)
                        pPage.pressGroupFromUI(this);
                }
            }
            if(bUp)
                onMessage(MESSAGE_CLICKUP,param1,param2,param3);
            return true;
        }
        else if(message == MESSAGE_CLICKUP && (mActive))
        {
  //          if(!pointInRect((int)param1,(int)param2))
  //              return false;
            if(!mEnable)
                return true;
            if(mLock && mGroup == 0 && !mCheck) {
                mCheck = true;
                return true;
            }
            if(!(mLock ) && !mActive)
                return true;
            if(mLock && mGroup>0)
                return true;
            if(mLock)
                mProject.clickAudio();
            if(m4Key)
            {
                if(m4KeyPos<0)
                {
                    if(mAlreadyShortPress)
                    {
                        mAlreadyShortPress = false;
                        m4KeyPos = 1;
                    }
                    else
                    {
                        mAlreadyShortPress = true;
                        m4KeyPos = 0;
                    }
                }
                else
                    m4KeyPos = -1;
            }
            onClick();
            mActive = false;
            mCheck = false;
            updateUI();

            m4KeyPos = -1;
            return true;
        }
        else if(message == MESSAGE_TIMER && mActive)
        {
            if(!m4Key)
                return false;
            long nowTick = System.currentTimeMillis();
            if((m4KeyPos == 2 || m4KeyPos == 3) && (nowTick-mTick)>60)
            {
                mTick = nowTick;
                onClick();;
            }
            else if(m4KeyPos<0 && (nowTick-mTick)>500)
            {
                mTick = nowTick;
                m4KeyPos = mAlreadyLongPress?3:2;
                mAlreadyLongPress = !mAlreadyLongPress;
                onClick();;
            }
        }

        return false;

    }

    public CFunction getFunction(int index)
    {
        return mFun[index];
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
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
    static String  S_PSWENABLE	= "PSWEnable";
    static String  S_PSWVALUE	= "PSWValue";
    static String  S_PSWEVERYTIME = "PSWEveryTime";
    static int  S_ImageIndex[] = {R.drawable._buttonimage0,R.drawable._buttonimage1,R.drawable._buttonimage2,R.drawable._buttonimage3
            ,R.drawable._buttonimage4,R.drawable._buttonimage5,R.drawable._buttonimage6,R.drawable._buttonimage7,R.drawable._buttonimage8
            ,R.drawable._buttonimage9,R.drawable._buttonimage10,R.drawable._buttonimage11,R.drawable._buttonimage12,R.drawable._buttonimage13};

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
        mFun[0] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION1));
        mFun[1] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION2));
        mFun[2] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION3));
        mFun[3] = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION4));
        mLock = Integer.parseInt(xmlData.getAttribute(S_LOCK)) == 1;
        mGroup = Integer.parseInt(xmlData.getAttribute(S_GROUP));
        m4Key = Integer.parseInt(xmlData.getAttribute(S_B4KEY)) == 1;
        //psw
        mEnablePSW = Integer.parseInt(xmlData.getAttribute(S_PSWENABLE)) == 1;
        mEveryTime = Integer.parseInt(xmlData.getAttribute(S_PSWEVERYTIME)) == 1;
        stringPSW = xmlData.getAttribute(S_PSWVALUE);

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
            imageActive = BitmapFactory.decodeResource(MyApplication.getAppContext().getResources(),S_ImageIndex[imageIndex]);
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
        if(m4Key && m4KeyPos>=0 && m4KeyPos<4)
            mProject.uiSend(mFun[m4KeyPos]);
        else if(!m4Key && mLock) {
            CFunction fun = mFun[mActive ? 1 : 0];
            mProject.uiSend(mFun[mActive ? 1 : 0]);
            if(fun != null && fun.codeData != null && fun.codeData.length == 8)
            {
                if((fun.codeData[0]&0xff) == 0xd2 && fun.codeData[1] == 0x2a)
                {
                    if(mOwner instanceof CUIPage)
                    {
                        CUIPage page = (CUIPage)mOwner;
                        page.onOffCondition(fun.codeData[6] == 1);
                    }
                    else if(mOwner instanceof CSubPage)
                    {
                        CSubPage page = (CSubPage) mOwner;
                        page.onOffCondition(fun.codeData[6] == 1);
                    }
                }
            }
        }
        else if(!m4Key && !mLock && !mActive)
            mProject.uiSend(mFun[0]);
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

    void checkPassword(){
        final EditText edt = new EditText(mProject.mView.getContext());
        edt.setId(R.id.password_editview);
        edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        edt.setSingleLine();
        FrameLayout container = new FrameLayout(mProject.mView.getContext());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin= 10; // remember to scale correctly
        edt.setLayoutParams(params);
        container.addView(edt);
        final AlertDialog d = new AlertDialog.Builder(mProject.mView.getContext()).setTitle(R.string.password_title)
                .setView(container)
                .setPositiveButton(R.string.dialog_ok, null)
                .setNegativeButton(R.string.dialog_cancel, null).create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String psw = edt.getText().toString();
                        if(psw.contentEquals(stringPSW))
                        {
                            mHavePsw = true;

                            mTick = System.currentTimeMillis();
                            if(!m4Key)
                                CUIButton.this.onClick();
                            else
                                m4KeyPos = -1;
                            mActive = true;

                            updateUI();
                            if(mLock && mGroup>0)
                            {
                                CUIPage pPage = (CUIPage)mOwner;
                                if(pPage != null)
                                    pPage.pressGroupFromUI(CUIButton.this);
                            }

                            if(!(mLock && mCheck) && !mActive)
                                return;
                            if(mLock && mGroup>0)
                                return;
                            if(m4Key)
                            {
                                if(m4KeyPos<0)
                                {
                                    if(mAlreadyShortPress)
                                    {
                                        mAlreadyShortPress = false;
                                        m4KeyPos = -1;
                                    }
                                    else
                                    {
                                        mAlreadyShortPress = true;
                                        m4KeyPos = 0;
                                    }
                                }
                                else
                                    m4KeyPos = -1;
                            }
                            CUIButton.this.onClick();
                            mActive = false;
                            mCheck = false;
                            updateUI();

                            m4KeyPos = -1;

                            dialog.dismiss();
                        }
                        else
                            return;
                    }
                });
            }
        });

        d.show();
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
            if(mActive||(mLock && mCheck)) {
                if(mBackgroundStyleActive == 0)
                    return;
                paint.setColor(colorBackgroundActive);
            }
            else {
                if (mBackgroundStyleInactive == 0)
                    return;
                paint.setColor(colorBackgroundInactive);
            }
        }
        else {
            if(mBackgroundStyleActive == 0)
                return;
            paint.setColor(colorBackgroundActive);
        }
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
                if(newPath == null)
                    return;
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

        WSUtil.drawText(dc,paint,rc,text,valign,align);
    }

    @Override
    public void updateDataFromBus(int bus,byte[] buf) {
        if(!mLock)
            return;
        if(!beingMsg && mFun[0] != null && mFun[0].codeData != null && mFun[0].mFunction == 0 && bus == mFun[0].mBus && mFun[0].codeData.length == buf.length)
        {
            if(Arrays.equals(buf,mFun[0].codeData))
            {
                setCheck(true);
                if(mGroup>0)
                {
                    if(mOwner instanceof CUIPage) {
                        CUIPage pPage = (CUIPage) mOwner;
                        if (pPage != null)
                            pPage.pressGroupFromUI(this);
                    }
                    else{
                        CSubPage pPage = (CSubPage) mOwner;
                        if (pPage != null)
                            pPage.pressGroupFromUI(this);

                    }
                }
                return;
            }
        }
        if(mGroup<=0 && mFun[1] != null && mFun[1].codeData != null && mFun[1].mFunction == 0 && bus == mFun[0].mBus && mFun[1].codeData.length == buf.length)
        {
            if(Arrays.equals(buf,mFun[1].codeData))
            {
                setCheck(false);
                return;
            }
        }

    }
}
