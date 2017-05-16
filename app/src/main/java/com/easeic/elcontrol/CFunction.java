package com.easeic.elcontrol;

/**
 * Created by sam on 2016/3/24.
 */
public class CFunction {
    public int mFunction = 0;
    public int mBus = 0;
    public int mEvent = 0;
    public int mTask = 0;
    public int mTiming = 0;
    public byte[] codeData = null;
    public boolean mRunning;

    public static CFunction functionFromString(String string){
        CFunction fun = new CFunction();
        fun.inputText(string);

        return fun;
    }

    private void inputText(String string)
    {
        if(string.length()<13)
            return;
        String[] strings = string.split(",");
        mFunction = Integer.parseInt(strings[0]);
        mBus = Integer.parseInt(strings[1]);
        if(Integer.parseInt(strings[2])>0)
            codeData = WSUtil.StringToByteArray(strings[3]);
        mEvent = Integer.parseInt(strings[4]);
        mTask = Integer.parseInt(strings[5]);
        mTiming = Integer.parseInt(strings[6]);
        mRunning = Integer.parseInt(strings[7])==0?false:true;
    }
}
