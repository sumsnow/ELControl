package com.easeic.elcontrol;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Vector;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIPage extends CUIBase {
    public Vector<CUIBase>  vecBases = new Vector<>();

    CUIBase mPressBase = null;
    Point   pressDownPoint = null;
    boolean mPressDown = false;

    private int mStyle;
    private int colorBack;
    private int colorBack2;
    private Bitmap imageBack;

    //override

    public static CUIPage pageCreateFromXML(Element xmlData,CProject project)
    {
        CUIPage page = new CUIPage();
        page.mProject = project;
        page.loadXML(xmlData);

        return page;
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


    final int POINTLENGTH = 100;

    @Override
    public Boolean onMessage(int message, float param1, float param2, float param3) {
        if(message == MESSAGE_CLICKUP && mPressBase != null)
        {
            mPressBase.onMessage(message,param1,param2,param3);
            mPressBase = null;
            mPressDown = false;
        }
        else if(message == MESSAGE_CLICKUP)
        {
            mPressDown = false;
        }
        else {
            if(message == MESSAGE_CLICKDOWN){
                mPressDown = true;
                pressDownPoint = new Point((int)param1,(int)param2);
            }
            for (int i = vecBases.size() - 1; i >= 0; i--) {
                if (vecBases.elementAt(i).onMessage(message, param1, param2, param3)) {
                    if (message == MESSAGE_CLICKDOWN) {
                        mPressBase = vecBases.elementAt(i);
                    } else if(message == MESSAGE_MOUSEMOVE)
                        mPressDown = false;
                    return true;
                }
            }
        }

        if(mPressDown && message == MESSAGE_MOUSEMOVE && pressDownPoint != null){
            int fx = (int)param1 - pressDownPoint.x;
            int fy = (int)param2 - pressDownPoint.y;
            int direct = -1;
            if(Math.abs(fx)>=Math.abs(fy))
            {
                if(fx >= POINTLENGTH)
                    direct = 0;
                else if(fx <= -POINTLENGTH)
                    direct = 1;
            }
            else
            {
                if(fy >= POINTLENGTH)
                    direct = 2;
                else if(fy <= -POINTLENGTH)
                    direct = 3;
            }

            if(switchPage(direct)) {
                mPressDown = false;
            }
        }

        return false;
    }

    boolean switchPage(int direct){
        switch (direct)
        {
            case 0:
            {
                int nPage = mLeftPage;
                if(mProject.getPage(nPage) != null){
                    mProject.mView.mDirect = direct;
                    mProject.setActivePage(nPage);
                }
            }
            break;
            case 1:
            {
                int nPage = mRightPage;
                if(mProject.getPage(nPage) != null){
                    mProject.mView.mDirect = direct;
                    mProject.setActivePage(nPage);
                }
            }
            break;
            case 2:
            {
                int nPage = mTopPage;
                if(mProject.getPage(nPage) != null){
                    mProject.mView.mDirect = direct;
                    mProject.setActivePage(nPage);
                }
            }
            break;
            case 3:
            {
                int nPage = mBottomPage;
                if(mProject.getPage(nPage) != null){
                    mProject.mView.mDirect = direct;
                    mProject.setActivePage(nPage);
                }
            }
            break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        drawPage(dc,rect);

        for(CUIBase btn:vecBases)
        {
            Rect rcBtn = btn.getRect();
            if(rcBtn.intersect(rect))
            {
                btn.onDraw(dc,rect);
            }
        }
    }

    public static final String S_PAGE =	"Page";
    public static final String S_NOTE = 	"Note";
    public static final String S_STYLE = "Style";
    public static final String S_CLR1 = 	"Color1";
    public static final String S_CLR2 = "Color2";
    public static final String S_IMAGE	 ="Image";
    public static final String S_LINKPAGE = "LinkPage";

    public int mLeftPage = 0;
    public int mRightPage = 0;
    public int mTopPage = 0;
    public int mBottomPage = 0;

    @Override
    public void loadXML(Element xmlData) {
        String tmp = xmlData.getAttribute(S_ID);
        if(tmp != null)
            mID = Integer.parseInt(tmp);
        mName = xmlData.getAttribute(S_NAME);
        tmp = xmlData.getAttribute(S_IMAGE);
        if(tmp != null && tmp.length()>0)
        {
            Point pt = mProject.getSize();
            imageBack = WSUtil.loadIOBitmap(mProject.getResFile(tmp),pt.x,pt.y);
        }
        tmp = xmlData.getAttribute(S_CLR1);
        if(tmp != null)
            colorBack = WSUtil.colorWinToAD(Integer.parseInt(tmp));
        tmp = xmlData.getAttribute(S_CLR2);
        if(tmp != null)
            colorBack2 = WSUtil.colorWinToAD(Integer.parseInt(tmp));

        mStyle = Integer.parseInt(xmlData.getAttribute(S_STYLE));

        try {
            String strLink = xmlData.getAttribute(S_LINKPAGE);
            if (strLink != null && strLink.length() > 0) {
                String[] strings = strLink.split(":");
                if (strings.length == 4) {
                    mLeftPage = Integer.parseInt(strings[0]);
                    mRightPage = Integer.parseInt(strings[1]);
                    mTopPage = Integer.parseInt(strings[2]);
                    mBottomPage = Integer.parseInt(strings[3]);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

        Log.i("page load",mName);
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

    @Override
    public Rect getRect() {
        if(mProject != null && mRect == null)
            mRect = new Rect(0,0,mProject.getSize().x,mProject.getSize().y);
        return super.getRect();
    }

    @Override
    public void onShow() {
        super.onShow();

        mPressBase = null;
        mPressDown = false;


        for (CUIBase item : vecBases
        ){
            item.onShow();
        }
    }

    //private method
    private void drawPage(Canvas dc, Rect rect){
        Rect rc = getRect();

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
                dc.drawARGB(255, Color.red(colorBack), Color.green(colorBack), Color.blue(colorBack));
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
                dc.drawARGB(255, Color.red(colorBack), Color.green(colorBack), Color.blue(colorBack));
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
                dc.drawARGB(255, Color.red(colorBack), Color.green(colorBack), Color.blue(colorBack));
                if(imageBack == null)
                    break;
                dc.drawBitmap(imageBack,new Rect(0,0,imageBack.getWidth(),imageBack.getHeight()),rc,null);
            }
            break;
        }//end switch

    }

    @Override
    public void updateDataFromBus(int bus,byte[] buf) {
        for (CUIBase item:vecBases
             ) {
            item.updateDataFromBus(bus,buf);
        }
    }
}
