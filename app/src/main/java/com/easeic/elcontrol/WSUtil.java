package com.easeic.elcontrol;


import java.util.Vector;
import java.io.*;
import java.nio.ByteBuffer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.BitmapFactory;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Size;

class Font {
    public int	textSize = 12;
    public boolean underline = false;
    public boolean italic = false;
    public boolean bold = false;

    public static Font fontFromString(String text)
    {
        if(text == null || text.length()<4)
            return null;
        Font font = new Font();
        String[] strings = text.split(",");
        font.textSize = Math.abs(Integer.parseInt(strings[0]))*72/96+1;
        font.underline = Integer.parseInt(strings[2]) == 1;
        font.bold = Integer.parseInt(strings[1]) == 700;
        font.italic = Integer.parseInt(strings[3]) == 1;

        return font;
    }

    void setFontToPaint(Paint paint){
        if(bold && italic)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD_ITALIC));
        else if(bold)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        else if(italic)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
        else
            paint.setTypeface(Typeface.DEFAULT);
        paint.setUnderlineText(underline);
        paint.setTextSize(textSize);
    }
}

public class WSUtil {
    public static Resources wsRes = null;
    public final static String BOOL_TRUE = "true";
    public final static String BOOL_FALSE = "false";

    public static  String backToReturnString(String src){
        return src.replaceAll("<br>","\n");
    }

    public static boolean stringToBoolean(String s){
        if(s == null)
            return false;
        return s.equals(BOOL_TRUE);
    }

    public static byte[] intToByteArray(int i) {
        byte[] b = new byte[4];
        b[3] = (byte) ( (i >>> 24) & 0xFF);
        b[2] = (byte) ( (i >>> 16) & 0xFF);
        b[1] = (byte) ( (i >>> 8) & 0xFF);
        b[0] = (byte) ( (i >>> 0) & 0xFF);
        return b;
    }

    public static int byteArrayToInt(byte b[]){
        return (b[0] & 0xff) | (b[1]& 0xff) << 8 | (b[2] & 0xff) << 16 | (b[3] & 0xff) << 24;
    }

    public static int byteArrayToInt(byte b[],int offset){
        return (b[0+offset] & 0xff) | (b[1+offset]& 0xff) << 8 | (b[2+offset] & 0xff) << 16 | (b[3+offset] & 0xff) << 24;
    }

    public static byte[] douleToByteArray(double db) throws IOException {
        ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
        DataOutputStream dOutput = new DataOutputStream(bOutput);

        dOutput.writeDouble(db);

        return bOutput.toByteArray();
    }

    public static double byteArrayToDouble(byte []b) throws IOException{
        ByteArrayInputStream bInput = new ByteArrayInputStream(b);
        DataInputStream dInput = new DataInputStream(bInput);

        return dInput.readDouble();
    }

    public static int HexToInt(String str){
        return Integer.parseInt(str, 16);
    }

    public static String IntToHex(int n){

        String str = Integer.toHexString(n);

        if(str.length() < 2)
            str = " " + str;

        return str;
    }
    public static byte[] StringToByteArray(String str) {
        if(str == null || str.length() < 1)
            return null;
        byte[] rtn = null;
        int i=0;

        String a[] = str.split(" ");

        if(a.length < 1)
            return null;

        rtn = new byte[a.length];

        for(i=0;i<a.length;++i){
            try{
                rtn[i] = (byte)HexToInt(a[i]);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return rtn;
    }

    public static String ByteArrayToString(byte[] datas) {
        String rtn = "";
        for(int i=0;i<datas.length;++i){
            rtn += IntToHex(datas[i] & 0xff);
            if(i < datas.length-1)
                rtn += " ";
        }
        return rtn;
    }

    public static String loadString(int deviceTransport){
        return wsRes.getString(deviceTransport);
    }

    public static String getFilename(byte[] data){
        if(data == null)
            return null;

        StringList sl = new StringList();
        try {
            sl.setDataStrings(data);
            if(sl.getCount() == 2){
                return sl.getAt(1);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return null;
    }

    public static Bitmap loadIOBitmap(String str,int width,int height){
        Bitmap ret = null;
        File file = new File(str);
        int sampleSize = 1;
        if(file.exists() && !file.isDirectory()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Config.RGB_565;

            options.inSampleSize = 2;
            while(options.inSampleSize < 10){
                try{
                    InputStream is = null;
                    try {
                        is = new FileInputStream(str);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return ret;
                    }
                    //Log.d("start loadbitmap...",str + " samplesize="+options.inSampleSize);
                    ret = BitmapFactory.decodeStream(is, null, options);
                    break;
                }catch(OutOfMemoryError ee){
                    ee.printStackTrace();
                    //Log.d("loadbitmap error",str + " samplesize="+options.inSampleSize);
                    options.inSampleSize <<= 1;
                    sampleSize = options.inSampleSize;
                }
            }
        }

        if(ret != null && (height*2<ret.getHeight() || width*2<ret.getWidth()) && height>0 && width>0)
        {
            int x = ret.getWidth()/width+1;
            int y = ret.getHeight()/height+1;
            int scale = x>y?x:y;
            if(scale<=sampleSize*4)
                return ret;
            sampleSize = scale/4;

            ret.recycle();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Config.RGB_565;

            options.inSampleSize = sampleSize;
            while(options.inSampleSize < 10){
                try{
                    InputStream is = null;
                    try {
                        is = new FileInputStream(str);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return ret;
                    }
                    //Log.d("start loadbitmap...",str + " samplesize="+options.inSampleSize);
                    ret = BitmapFactory.decodeStream(is, null, options);
                    break;
                }catch(OutOfMemoryError ee){
                    ee.printStackTrace();
                    //Log.d("loadbitmap error",str + " samplesize="+options.inSampleSize);
                    options.inSampleSize <<= 1;
                    sampleSize = options.inSampleSize;
                }
            }
        }
        return ret;
    }

    public static Bitmap loadIOBitmap(String str){
        Bitmap ret = null;
        File file = new File(str);
        if(file.exists() && !file.isDirectory()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Config.RGB_565;

            options.inSampleSize = 1;
            while(options.inSampleSize < 10){
                try{
                    InputStream is = null;
                    try {
                        is = new FileInputStream(str);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                        return ret;
                    }
                    //Log.d("start loadbitmap...",str + " samplesize="+options.inSampleSize);
                    ret = BitmapFactory.decodeStream(is, null, options);
                    break;
                }catch(OutOfMemoryError ee){
                    ee.printStackTrace();
                    //Log.d("loadbitmap error",str + " samplesize="+options.inSampleSize);
                    options.inSampleSize <<= 1;
                }
            }
        }

        return ret;
    }

    public static Bitmap transparentBitmap(Bitmap bmp){
        if(bmp == null)
            return null;
        Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
        int color = bmp.getPixel(0, 0);
        for(int i=0;i<bmp.getHeight();++i)
            for(int j=0;j<bmp.getWidth();j++){
                int newColor = bmp.getPixel(j, i);
                if((newColor & 0x00ffffff) == (color & 0x00ffffff))
                    newBmp.setPixel(j, i, 0);
                else
                    newBmp.setPixel(j, i, newColor | 0xff000000);
            }
        bmp.recycle();

        return newBmp;
    }

    public static int colorWinToAD(int color){
        return Color.argb(0xff,	Color.blue(color), Color.green(color), Color.red(color));
    }

    public static void drawText(Canvas canvas,Paint paint,Rect rcText,String text,boolean vcenter,int align){
        //align = 0 left,=1,center,=2,right
        if(text == null || text.length() < 1)
            return ;

        FontMetrics fm = paint.getFontMetrics();
        int fontHeight = (int)Math.ceil(fm.descent - fm.ascent);

        String[] items = text.split("\n");
        Vector<String> vecItems = new Vector<String>();

        for(String item:items){
            int pos = 0;
            for(int i=1;i<=item.length();++i){
                String str = item.substring(pos,i);
                int width = (int)paint.measureText(str);
                if(width >= rcText.width()){
                    vecItems.add(item.substring(pos,i-1));
                    pos = i-1;
                }
                if(i == (item.length())){
                    vecItems.add(item.substring(pos));
                }
            }
        }
                //draw
        int top = 0;
        int left = rcText.left;
        if(vcenter){
            int needHeight = fontHeight * vecItems.size();
            if(needHeight < rcText.height())
                top = rcText.top + (rcText.height() - needHeight)/2 - (int)fm.ascent;
        }
        else
            top = rcText.top + fontHeight/2;

        if(align == 1)
            left = rcText.left + rcText.width()/2;
        else if(align == 2)
            left = rcText.right-rcText.width();

        for(int i=0;i<vecItems.size();++i){
            int y = (i)*fontHeight+top;
            if((y) < rcText.bottom){
                canvas.drawText(vecItems.get(i), left, y, paint);
            }
            else
                break;
        }
    }

    /*
    valign
     */
    public static void drawText(Canvas canvas,Paint paint,Rect rcText,String text,int valign,int align){
        //align = 0 left,=1,center,=2,right
        if(text == null || text.length() < 1)
            return ;

        FontMetrics fm = paint.getFontMetrics();
        int fontHeight = (int)Math.ceil(fm.descent - fm.ascent);

        String[] items = text.split("\n");
        Vector<String> vecItems = new Vector<String>();

        for(String item:items){
            int pos = 0;
            for(int i=1;i<=item.length();++i){
                String str = item.substring(pos,i);
                int width = (int)paint.measureText(str);
                if(width >= rcText.width()){
                    vecItems.add(item.substring(pos,i-1));
                    pos = i-1;
                }
                if(i == (item.length())){
                    vecItems.add(item.substring(pos));
                }
            }
        }

        //draw
        int top = rcText.top + fontHeight/2;;
        int left = rcText.left;
        if(valign != 0){
            int needHeight = fontHeight * vecItems.size();
            if(needHeight < rcText.height())
                if(valign == 1)
                    top = rcText.top + (rcText.height() - needHeight)/2 - (int)fm.ascent;
                else
                    top = rcText.top + (rcText.height() - needHeight) - (int)fm.ascent;
        }

        for(int i=0;i<vecItems.size();++i){
            int y = (i)*fontHeight+top;
            if((y) < rcText.bottom){
                int width = (int) paint.measureText(vecItems.get(i));
                if(align != 0) {
                    if (align == 1)
                        left = rcText.left + (rcText.width() - width) / 2;
                    else
                        left = rcText.right - width;
                }
                canvas.drawText(vecItems.get(i), left, y, paint);
            }
            else
                break;
        }
    }
    //gradient fill
    //gradientType = 0,her;=1,ver;=2,her mirror,=3,ver mirror;=4,circal;
    public static void gradientFill(Canvas dc,Rect rc,int colorFrom,int colorTo,int gradientType)
    {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int w = 0;
        int h = 0;
        Shader.TileMode tm = Shader.TileMode.CLAMP;
        switch(gradientType)
        {
            case 0:
            {
                w = rc.width();
                h = 0;
            }
            break;
            case 1:
                h = rc.height();
                break;
            case 2:
                w = rc.width()/2+rc.left;
                tm = Shader.TileMode.MIRROR;
                h=rc.top;
                break;
            case 3:
                w = rc.left;
                h = rc.height()/2+rc.top;
                tm = Shader.TileMode.MIRROR;
                break;
        }
        if(gradientType ==4) {
            int radius = (int)Math.sqrt(rc.width()*rc.width()/4.+rc.height()*rc.height()/4.);
            paint.setShader(new RadialGradient(rc.centerX(),rc.centerY(),radius,colorFrom,colorTo,tm));
        }
        else
            paint.setShader(new LinearGradient(rc.left,rc.top,w,h,colorFrom,colorTo,tm));
        dc.drawRect(rc, paint);
    }
    //gradient fill circle
    public static void gradientFillCircle(Canvas dc,Rect rc,int colorFrom,int colorTo)
    {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader.TileMode tm = Shader.TileMode.CLAMP;
        float radius = (float)Math.sqrt(rc.width()*rc.width()/4.+rc.height()*rc.height()/4.);
        int clrs[] = {colorFrom,colorFrom,colorTo};
        float poss[] = {0,0.9f,1};
        paint.setShader(new RadialGradient(rc.centerX(),rc.centerY(),radius,clrs,poss,tm));
        dc.drawCircle(rc.centerX(),rc.centerY(),radius,paint);
    }
};



class StringList{
    Vector<String> vectorStrings;

    public StringList(){
        vectorStrings = new Vector<String>();
    }

    void setDataStrings(byte pDatas[]) throws UnsupportedEncodingException{
        clear();

        if(pDatas == null || pDatas.length % 2 != 0)
            return;

        int i = 0;
        int offset = 0;
        for(;i<pDatas.length;i+=2){
            if(pDatas[i] == 0 && pDatas[i+1] == 0){
                byte[] newBytes = new byte[i-offset+2];
                newBytes[0] = -1;
                newBytes[1] = -2;
                System.arraycopy(pDatas,offset,newBytes,2,i-offset);

                offset = i+2;

                vectorStrings.add(new String(newBytes,"UTF-16"));
            }
        }
    }

    byte[] getStringsData() throws UnsupportedEncodingException{
        if(vectorStrings.size()<1)
            return null;

        int i = 0;
        byte []rtn = null;
        for(i=0;i<vectorStrings.size();++i){
            String str = vectorStrings.get(i);
            byte []byteString = str.getBytes("UTF-16");

            byte []newBytes = null;
            if(rtn == null){
                newBytes = new byte[byteString.length];
                newBytes[byteString.length-2] = 0;
                newBytes[byteString.length-1] = 0;
                System.arraycopy(byteString, 2, newBytes, 0, byteString.length-2);
            }
            else{
                newBytes = new byte[byteString.length+rtn.length];
                System.arraycopy(rtn, 0, newBytes, 0, rtn.length);
                System.arraycopy(byteString, 2, newBytes, rtn.length, byteString.length-2);

                newBytes[rtn.length+byteString.length-2] = 0;
                newBytes[rtn.length+byteString.length-1] = 0;
            }

            rtn = newBytes;
        }

        return rtn;
    }

    void clear(){
        vectorStrings.clear();
    }

    // 取得字符串计数
    int getCount(){
        return vectorStrings.size();
    }

    // 取得指定位置的字符串，失败返回NULL
    String getAt(int index){
        return vectorStrings.get(index);
    }

    // 设置指定位置的字符串，失败返回-1
    boolean setAt( int nIndex, String stringItem){
        if(nIndex < vectorStrings.size()){
            vectorStrings.remove(nIndex);
        }

        vectorStrings.add(nIndex, stringItem);
        return true;
    }

    // 删除指定位置的字符串
    boolean delete( int nIndex ){
        if(nIndex <0 || nIndex >= vectorStrings.size())
            return false;
        vectorStrings.remove(nIndex);
        return true;
    }
};

class BinaryList{

    public Vector<byte[]>	vectorBinaries;

    public BinaryList(){
        vectorBinaries = new Vector<byte[]>();
    }

    public void setData(byte[] pDatas) throws IOException{
        clear();

        ByteArrayInputStream bInput = new ByteArrayInputStream(pDatas);
        DataInputStream dInput = new DataInputStream(bInput);
        byte[] ba = new byte[4];
        dInput.read(ba);
        int binCount = WSUtil.byteArrayToInt(ba);
        int i = 0;
        for(i=0;i<binCount;++i)
        {
            byte b = dInput.readByte();
            int dataLength = b & 0xff;

            byte[] bData = new byte[dataLength];
            dInput.read(bData);

            vectorBinaries.add(bData);
        }
    }

    public byte[] getData() throws IOException{
        if(vectorBinaries.size() < 1)
            return null;

        ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
        DataOutputStream dOutput = new DataOutputStream(bOutput);

        dOutput.write(WSUtil.intToByteArray(vectorBinaries.size()));

        int i=0;
        for(i=0;i<vectorBinaries.size();++i){
            byte[] bData = vectorBinaries.get(i);

            dOutput.writeByte(bData.length);
            dOutput.write(bData);
        }

        return bOutput.toByteArray();
    }

    // 取得代码个数
    public int getCount(){
        return vectorBinaries.size();
    }

    // 取得指定位置的代码，失败返回NULL
    public byte[] getAt(int index){
        return vectorBinaries.get(index);
    }

    // 设置指定位置的字符串，失败返回FALSE
    public void setAt(int index,byte[] datas){
        if(index < 0 || index >= vectorBinaries.size())
            vectorBinaries.add(datas);
        else{
            vectorBinaries.remove(index);
            vectorBinaries.add(datas);
        }
    }

    //
    public void insert(int index,byte[] pData){
        vectorBinaries.add(index,pData);
    }

    public void delete(int index){
        if(index >= vectorBinaries.size() || index < 0)
            return;

        vectorBinaries.remove(index);
    }

    public void clear(){
        vectorBinaries.clear();
    }
};