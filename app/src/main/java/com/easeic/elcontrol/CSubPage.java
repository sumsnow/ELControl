package com.easeic.elcontrol;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Vector;

/**
 * Created by sam on 2016/3/25.
 */
public class CSubPage extends CUIBase {
    public Vector<CUIBase>  vecBases = new Vector<>();

    CUIBase mPressBase = null;


    Point   mSize;

    private int mStyle;
    private int colorBack;
    private int colorBack2;
    private Bitmap imageBack;

    private Bitmap  mBmpCanvas;
    private Canvas mBufCanvas;
    //override

    public static CSubPage pageCreateFromXML(Element xmlData,CProject project)
    {
        CSubPage page = new CSubPage();
        page.mProject = project;
        page.loadXML(xmlData);

        return page;
    }

    public void onOffCondition(Boolean b)
    {
        for (CUIBase item : vecBases
                ){
            if(item instanceof CUIButton)
            {
                CUIButton btn = (CUIButton)item;
                for (int i=0;i<2;i++)
                {
                    CFunction fun = btn.getFunction(i);
                    if(fun == null || fun.codeData == null || fun.codeData.length != 8
                            || (fun.codeData[0]&0xff) != 0xd2 || (fun.codeData[1] != 0x28 && fun.codeData[1] != 0x3f))
                        continue;;
                    btn.mEnable = b;

                    break;
                }
            }
        }
    }

    public void pressGroupFromUI(CUIBase ui)
    {
        if(!(ui instanceof CUIButton))
        {
            return;
        }

        CUIButton src = (CUIButton)ui;
        for (CUIBase item:vecBases)
        {
            if(item instanceof CUIButton)
            {
                CUIButton btn = (CUIButton)item;
                if(btn == src)
                    continue;;
                if(btn.mGroup == src.mGroup)
                    btn.setCheck(false);
            }
        }
    }


    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKUP && mPressBase != null)
        {
            mPressBase.onMessage(message,param1,param2,param3);
            mPressBase = null;
        }
        if(message != MESSAGE_TIMER)
        {
            Rect rc = getRect();
            if(!rc.contains((int)param1,(int)param2))
            {
                return false;
            }
        }
        param1 = PhyToLogX((int)param1);
        param2 = PhyToLogY((int)param2);
        for (int i=vecBases.size()-1;i>=0;i--){
            if(vecBases.elementAt(i).onMessage(message,param1,param2,param3))
            {
                if(message == MESSAGE_CLICKDOWN)
                {
                    mPressBase = vecBases.elementAt(i);
                }
            }
                return true;
        }

        return false;
    }

    public int PhyToLogX(int x){
        x -= mRect.left;
        return x*mSize.x/mRect.width();
    }

    public int PhyToLogY(int y){
        y -= mRect.top;
        return y*mSize.y/mRect.height();
    }

    public Point PhyToLog(Point pt){
        return new Point(PhyToLogX(pt.x),PhyToLogY(pt.y));
    }

    public Rect PhyToLog(Rect rect){
        return new Rect(PhyToLogX(rect.left),PhyToLogY(rect.top),PhyToLogX(rect.right),PhyToLogY(rect.bottom));
    }

    public int LogToPhyX(int x){
        return x*mRect.width()/mSize.x+mRect.left;
    }

    public int LogToPhyY(int y){
        return y*mRect.height()/mSize.y+mRect.top;
    }

    public Point LogToPhy(Point pt){
        return new Point(LogToPhyX(pt.x),LogToPhyY(pt.y));
    }

    public Rect LogToPhy(Rect rect){
        return new Rect(LogToPhyX(rect.left),LogToPhyY(rect.top),LogToPhyX(rect.right),LogToPhyY(rect.bottom));
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        Rect rc = new Rect(rect);
        if(!rc.intersect(mRect))
            return;
        if (mBmpCanvas == null) {
            Point pt = mSize;
            mBmpCanvas = Bitmap.createBitmap(pt.x, pt.y,
                    Bitmap.Config.ARGB_8888);
        }

        if (mBufCanvas == null)
            mBufCanvas = new Canvas(mBmpCanvas);

        Rect realRc = PhyToLog(rc);

        if(mStyle> 5)
        {
            if(mOwner instanceof CSubPage)
            {
                CSubPage page = (CSubPage)mOwner;
                mBufCanvas.drawBitmap(page.mBmpCanvas,mRect,new Rect(0,0,mSize.x,mSize.y),null);
            }
            else
            {
                mBufCanvas.drawBitmap(mProject.mView.mBmpCanvas,mRect,new Rect(0,0,mSize.x,mSize.y),null);
            }
        }

        drawPage(mBufCanvas,rc);

        for(CUIBase btn:vecBases)
        {
            Rect rcBtn = btn.getRect();
            if(rcBtn.intersect(realRc))
            {
                btn.onDraw(mBufCanvas,realRc);
            }
        }

        dc.drawBitmap(mBmpCanvas,realRc,rc,null);
    }

    public static final String S_PAGE =	"Page";
    public static final String S_NOTE = 	"Note";
    public static final String S_STYLE = "Style";
    public static final String S_CLR1 = 	"Color1";
    public static final String S_CLR2 = "Color2";
    public static final String S_IMAGE	 ="Image";
    public static final String S_SIZE = "Size";

    @Override
    public void onShow() {
        for (CUIBase item :
                vecBases) {
            item.onShow();
        }
    }

    @Override
    public void loadXML(Element xmlData) {
        String stringSize = xmlData.getAttribute(S_SIZE);
        if(stringSize != null)
        {
            String[] strings = stringSize.split("X");
            if(strings.length == 2){
                mSize = new Point(Integer.parseInt(strings[0]),Integer.parseInt(strings[1]));
            }
        }
        String tmp = xmlData.getAttribute(S_IMAGE);
        if(tmp != null && tmp.length()>0)
        {
            imageBack = WSUtil.loadIOBitmap(mProject.getResFile(tmp),mSize.x,mSize.y);
        }
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        if(mRect.width()<1)
            mRect.right = mRect.left+1;
        if(mRect.height()<1)
            mRect.bottom = mRect.top+1;
        tmp = xmlData.getAttribute(S_CLR1);
        if(tmp != null)
            colorBack = WSUtil.colorWinToAD(Integer.parseInt(tmp));
        tmp = xmlData.getAttribute(S_CLR2);
        if(tmp != null)
            colorBack2 = WSUtil.colorWinToAD(Integer.parseInt(tmp));

        mStyle = Integer.parseInt(xmlData.getAttribute(S_STYLE));

        //children
        NodeList list = xmlData.getChildNodes();
        if(list == null)
            return ;
        for (int i=0;i<list.getLength();i++)
        {
            if(list.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                CUIBase item = CUIFactory.createUIFromXML((Element)list.item(i),this,mProject);
                if(item != null)
                    vecBases.add(item);
            }
        }

        onOffCondition(false);
    }

    @Override
    public Rect getRect() {
        return mRect;
    }

    //private method
    private void drawPage(Canvas dc, Rect rect){
        Rect rc = new Rect(0,0,mSize.x,mSize.y);

        switch (mStyle)
        {
            case 0:
            {
                dc.drawARGB(255, Color.red(colorBack), Color.green(colorBack), Color.blue(colorBack));

            }//end case
            break;
            case 1:
            {
                WSUtil.gradientFill(dc,rc,colorBack,colorBack2,0);
            }
            break;
            case 2:
                WSUtil.gradientFill(dc,rc,colorBack2,colorBack,0);
                break;
            case 3:
                WSUtil.gradientFill(dc,rc,colorBack,colorBack2,1);
                break;
            case 4:
                WSUtil.gradientFill(dc,rc,colorBack2,colorBack,1);
                break;
            case 5:
                WSUtil.gradientFill(dc,rc,colorBack,colorBack2,4);
                break;
            case 6:
            {
                //bitmap
                if(imageBack == null)
                    break;

                int nSrcX,nSrcY,nTarX,nTarY,nW,nH;
                if(rc.width()>=imageBack.getWidth())
                {
                    nTarX = (rc.width()-imageBack.getWidth())/2;
                    nSrcX = 0;
                    nW = imageBack.getWidth();
                }
                else
                {
                    nSrcX = (imageBack.getWidth()-rc.width())/2;
                    nTarX = 0;
                    nW = rc.width();
                }
                if(rc.height()>=imageBack.getHeight())
                {
                    nTarY = (rc.height()-imageBack.getHeight())/2;
                    nSrcY = 0;
                    nH = imageBack.getHeight();
                }
                else
                {
                    nSrcY = (imageBack.getHeight() - rc.height())/2;
                    nTarY = 0;
                    nH = rc.height();
                }

                dc.drawBitmap(imageBack,nTarX,nTarY,null);
            }
            break;
            case 7:
            {
                if(imageBack == null)
                    break;
                int srcX=0,srcY = 0;
                while(srcY<rc.bottom){
                    srcX = 0;
                    int h = (rc.bottom-srcY)>imageBack.getHeight()?imageBack.getHeight():(rc.bottom-srcY);
                    while(srcX<rc.right)
                    {
                        int w = (rc.right-srcX)>imageBack.getWidth()?imageBack.getWidth():(rc.right-srcX);
                        dc.drawBitmap(imageBack,srcX,srcY,null);
                        srcX += w;
                    }

                    srcY += h;
                }
            }
            break;
            case 8:
            {
                if(imageBack == null)
                    break;
                dc.drawBitmap(imageBack,new Rect(0,0,imageBack.getWidth(),imageBack.getHeight()),rc,null);
            }
            break;
        }//end switch

    }

    @Override
    public void updateDataFromBus(int bus,byte[] buf) {
        for(CUIBase item:vecBases){
            item.updateDataFromBus(bus,buf);
        }
    }
}
