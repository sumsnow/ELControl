package com.easeic.elcontrol;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import android.text.format.Time;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by sam on 2016/3/25.
 */
public class CUIClock extends CUIBase {

    static int days_of_month_1[]={31,28,31,30,31,30,31,31,30,31,30,31};
    static int  days_of_month_2[]={31,29,31,30,31,30,31,31,30,31,30,31};
    static double Degs =  57.2957795130823;
    static double Rads = 1.74532925199433E-02;
    static double Tpi = 6.28318530717958;//2pi=3.1415926*2//
   static double alt = -0.833;//日出日落时太阳的位置

    static int leap_year(int year){
        if(((year%400==0) || (year%100!=0) && (year%4==0))) return 1;
        else return 0;
    }

    static int DaysFrom2000(int Year,int  Month,int Day)
    {        int i,a=0;
        for(i=2000;i<Year;i++){
            if(leap_year(i) != 0) a=a+366;
            else a=a+365;
        }
        if(leap_year(Year)!= 0){
            for(i=0;i<Month-1;i++){
                a=a+days_of_month_2[i];
            }
        }
        else {
            for(i=0;i<Month-1;i++){
                a=a+days_of_month_1[i];
            }
        }
        a=a+Day;
        return a;
    }

    static double Range360(double x)
    {
        return x-360*Math.floor(x/360);
    }
//计算指定日期的日出日落时间
//Day =从2000年到计算日的天数
//RiseSet: 0＝日出,  1＝日落?
    //东经0-180 为正数,西经0-180为负数
    //北纬0-90为正数,南纬0-90为负数
    //北南  纬计算范围适用于//北纬  0-60为正数,南纬0-60为负数

    static double SunRiseSet(double  Day ,double latitude,double Longitude,int TimeZone,int  RiseSet)
    {
        double Sinalt,Gha,Lambda,Delta,T,C,Days,Utold,Utnew,Utold_p;
        double Sinphi,Cosphi,L,G,E,Obl,Signt,Act;

        //  u32 Utold_p,Utnew_p;
        Utold=180;
        Utnew=0;
        Sinalt=Math.sin(alt*Rads);
        Sinphi=Math.sin(Rads * latitude);
        Cosphi=Math.cos(Rads * latitude);

        while(Math.abs(Utold - Utnew)>0.1){//Utold-Utnew的绝对值
            // wdt_reset();
            //  Days=Day+Utold/360;
            //T=Days/36525;
            T=(Day+Utold/360)/36525;//求格林威治时间公元2000年1月1日到计算日的世纪数t
            L=Range360(280.46+36000.77*T);//求太阳的平黄径
            G=357.528+35999.05*T;//求太阳的平近点角
            Lambda=L+1.915*Math.sin(Rads * G)+0.02*Math.sin(Rads * 2 * G);//求黄道经度
            //Obl=23.4393-0.13*T;
            Obl=23.4393-0.013*T;//求地球倾角
            Delta=Degs*Math.asin(Math.sin(Rads * Obl) * Math.sin(Rads * Lambda));//求太阳偏差

            E=-1.915*Math.sin(Rads * G)-0.02*Math.sin(Rads * 2 * G)+2.466*Math.sin(Rads * 2 * Lambda)-0.053*Math.sin(Rads * 4 * Lambda);
            Gha=Utold-180+E;//求格林威治时间的太阳时间角GHA


            C=(Sinalt-Sinphi*Math.sin(Rads * Delta))/(Cosphi*Math.cos(Rads * Delta));
            Act=Degs*Math.acos(C);//求修正值e
            if(C>1)
                Act=0;
            if(C<-1)
                Act=180;
            if(RiseSet!=0)//

                Signt=-1;
            else
                Signt=1;

            //  Utold_p=Utold;
            Utnew=Range360(Utold-(Gha+Longitude+Signt*Act));
            Utold=Utnew;

        }

        Utnew=Utnew/15+TimeZone;
        if(Utnew<0)
            Utnew+=24;
        else   if(Utnew>=24)
            Utnew-=24;

        //   Utnew=Utnew*100;
        //	 test_send32((u32)(Utnew),0xac);

        return Utnew;
    }

    /*
    Param:
        SumSet_Rise    0＝日出,  1＝日落?
     */
    static int   Astronomy_Calculate(CProject pProject,int Year,int  Month,int Day,int   offset_val,int offset_sign,int SunSet_Rise)
    {
        int  Astronomy_Hour, Astronomy_Minute;
        int dats=0;
        double Time=0,Real_offset_val=0;

        Real_offset_val=(double)offset_val/60;
        if(offset_sign==0)
            Real_offset_val*=-1;

        dats =DaysFrom2000(Year,Month,Day);
        double dbLong = (pProject.m_location.m_nLongAngle+pProject.m_location.m_nLongPoint/60.0)*(pProject.m_location.m_bLongtitude?1:-1);
        double dbLat = (pProject.m_location.m_nLatAngle+pProject.m_location.m_nLatPoint/60.0)*(pProject.m_location.m_bLatitude?-1:1);
        int nGMT = pProject.m_location.m_nTimeZone*(pProject.m_location.m_bTimeZone?1:-1);
        Time=SunRiseSet(dats, dbLat, dbLong, nGMT, SunSet_Rise);//0＝日出1＝日落，--北京
        Time+=Real_offset_val;

        Astronomy_Hour=(int )Time;
        Astronomy_Minute=(int)((Time-Astronomy_Hour)*60);

        return (Astronomy_Hour*60+Astronomy_Minute)*60;
    }

    public static int getSunriseTiem(CProject project)
    {
        Date sunDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(sunDate);

        return Astronomy_Calculate(project,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),0,0,0);
    }

    public static int getSunsetTiem(CProject project)
    {
        Date sunDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(sunDate);

        return Astronomy_Calculate(project,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),0,0,1);
    }

    //
    public int mStyle;
    public boolean mShowTime;
    public boolean mShowSecond;
    public boolean mShow24Hour;
    public boolean mShowDayofweek;
    public boolean mShowFullname;
    public boolean mShowDate;
    public int mDateStyle;
    public boolean mShow4Year;
    public boolean mShowMonthname;
    public boolean mUseSperator;
    public int mAlign;
    public int colorText;
    public Font mFont;
    public CFunction mFun;
    public int mPage = 0;

    private long mTick = 0;
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
        else if(message == MESSAGE_TIMER && (System.currentTimeMillis()-mTick)>500)
        {
            mTick = System.currentTimeMillis();
            updateUI();;
        }
        return false;
    }

    @Override
    public void onDraw(Canvas dc, Rect rect) {
        String text = clockFormat();
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

        WSUtil.drawText(dc,paint, getRect(), text, valign, align);
    }

    private static String S_CLOCKSTYLE = "ClockStyle";
    private static String S_SHOWTIME = "ShowTime";
    private static String S_SHOWSECOND = "ShowSecond";
    private static String S_SHOW24HOUR = "Show24Hour";
    private static String S_SHOWDAYOFWEEK = "ShowDayofWeek";
    private static String S_SHOWFULLNAME = "ShowFullname";
    private static String S_SHOWDATE = "ShowDate";
    private static String S_DATESTYLE	= "DateStyle";
    private static String S_MONTHNAME = 	"MonthName";
    private static String S_USESPERATOR = "UseSeprator";
    private static String S_SHOW4YEAR	= "Show4Year";
    private static String S_ALIGN	= "Align";
    private static String S_COLOR =	"Color";
    private static String S_LINKPAGE	= "LinkPage";
    private static String S_FUNCTION	= "Function";
    private static String S_FONT = "font";
    @Override
    public void loadXML(Element xmlData) {
        mRect = rectFromString(xmlData.getAttribute(S_RECT));
        mAlign = Integer.parseInt(xmlData.getAttribute(S_ALIGN));
        colorText = WSUtil.colorWinToAD(Integer.parseInt(xmlData.getAttribute(S_COLOR)));
        mFont = Font.fontFromString(xmlData.getAttribute(S_FONT));
        mStyle = Integer.parseInt(xmlData.getAttribute(S_CLOCKSTYLE));
        mShowTime = Integer.parseInt(xmlData.getAttribute(S_SHOWTIME)) == 1;
        mShow24Hour = Integer.parseInt(xmlData.getAttribute(S_SHOW24HOUR)) == 1;
        mShowSecond = Integer.parseInt(xmlData.getAttribute(S_SHOWSECOND)) == 1;
        mShowDayofweek = Integer.parseInt(xmlData.getAttribute(S_SHOWDAYOFWEEK)) == 1;
        mShowFullname = Integer.parseInt(xmlData.getAttribute(S_SHOWFULLNAME)) == 1;
        mShow4Year = Integer.parseInt(xmlData.getAttribute(S_SHOW4YEAR)) == 1;
        mShowDate = Integer.parseInt(xmlData.getAttribute(S_SHOWDATE)) == 1;
        mDateStyle = Integer.parseInt(xmlData.getAttribute(S_DATESTYLE));
        mShowMonthname = Integer.parseInt(xmlData.getAttribute(S_MONTHNAME)) == 1;
        mUseSperator = Integer.parseInt(xmlData.getAttribute(S_USESPERATOR)) == 1;
        if(xmlData.getAttribute(S_LINKPAGE) != null && xmlData.getAttribute(S_LINKPAGE).length()>0)
            mPage = Integer.parseInt(xmlData.getAttribute(S_LINKPAGE));
        mFun = CFunction.functionFromString(xmlData.getAttribute(S_FUNCTION));
    }

    //private
    private String clockFormat(){
        switch (mStyle)
        {
            case 0: {
                String stringFormat = new String();

                if (mShowDayofweek)
                    if (mShowFullname)
                        stringFormat += "EEEE ";
                    else
                        stringFormat += "E ";

                if (mShowDate) {
                    if (mUseSperator) {
                        switch (mDateStyle) {
                            case 0: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "dd/MMM/yyyy ";
                                else if (mShow4Year)
                                    stringFormat += "dd/M/yyyy ";
                                else if (mShowMonthname)
                                    stringFormat += "dd/MMM/yy ";
                                else
                                    stringFormat += "dd/M/yy ";
                            }
                            break;
                            case 1: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "MMM/dd/yyyy ";
                                else if (mShow4Year)
                                    stringFormat += "M/dd/yyyy ";
                                else if (mShowMonthname)
                                    stringFormat += "MMM/dd/yy ";
                                else
                                    stringFormat += "M/dd/yy ";
                            }
                            break;
                            case 2: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "yyyy/MMM/dd ";
                                else if (mShow4Year)
                                    stringFormat += "yyyy/M/dd ";
                                else if (mShowMonthname)
                                    stringFormat += "yy/MMM/dd ";
                                else
                                    stringFormat += "yy/M/dd ";
                            }
                            break;
                        }//end datastyle
                    }//end show usesperator
                    else
                    {

                        switch (mDateStyle) {
                            case 0: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "dd MMM yyyy ";
                                else if (mShow4Year)
                                    stringFormat += "dd M yyyy ";
                                else if (mShowMonthname)
                                    stringFormat += "dd MMM yy ";
                                else
                                    stringFormat += "dd M yy ";
                            }
                            break;
                            case 1: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "MMM dd yyyy ";
                                else if (mShow4Year)
                                    stringFormat += "M dd yyyy ";
                                else if (mShowMonthname)
                                    stringFormat += "MMM dd yy ";
                                else
                                    stringFormat += "M dd yy ";
                            }
                            break;
                            case 2: {
                                if (mShow4Year && mShowMonthname)
                                    stringFormat += "yyyy MMM dd ";
                                else if (mShow4Year)
                                    stringFormat += "yyyy M dd ";
                                else if (mShowMonthname)
                                    stringFormat += "yy MMM dd ";
                                else
                                    stringFormat += "yy M dd ";
                            }
                            break;
                        }//end datastyle
                    }
                }//end showdate
                if (mShowTime) {
                    if (mShow24Hour) {
                        if (mShowSecond)
                            stringFormat += "HH:mm:ss ";
                        else
                            stringFormat += "HH:mm";
                    } else {
                        if (mShowSecond)
                            stringFormat += "hh:mm:ss ";
                        else
                            stringFormat += "hh:mm ";
                        stringFormat += "a ";
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat(stringFormat);
                return sdf.format(new Date());
            }//end case 0
            case 1:
            {
                String stringFormat = new String();

                if(mShow24Hour)
                    stringFormat = "HH:mm";
                else
                    stringFormat = "hh:mm";
                if(mShowSecond)
                    stringFormat += ":ss ";
                if(!mShow24Hour)
                    stringFormat += " a";

                Date sunDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(sunDate);

                int nTime = Astronomy_Calculate(mProject,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),0,0,0);
                cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),nTime/3600,nTime%3600/60,nTime%60);
                SimpleDateFormat sdf = new SimpleDateFormat(stringFormat);
                return sdf.format(cal.getTime());
            }
            case 2:
            {
                String stringFormat = new String();

                if(mShow24Hour)
                    stringFormat = "HH:mm";
                else
                    stringFormat = "hh:mm";
                if(mShowSecond)
                    stringFormat += ":ss ";
                if(!mShow24Hour)
                    stringFormat += " a";

                Date sunDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(sunDate);

                int nTime = Astronomy_Calculate(mProject,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),0,0,1);
                cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),nTime/3600,nTime%3600/60,nTime%60);
                SimpleDateFormat sdf = new SimpleDateFormat(stringFormat);
                return sdf.format(cal.getTime());
            }
        }

        return null;
    }

    private  void onClick(){
        if(mFun != null)
            mProject.uiSend(mFun);
        if(mPage>0)
            mProject.setActivePage(mPage);
    }
}
