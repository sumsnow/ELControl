package com.easeic.elcontrol;

/**
 * Created by sam on 2016/4/2.
 */
public class CPageTaskNode {
    public byte[]   mCode;
    public int      mDelay = 0;
    public int      mBus = 0;
    public int      mJump = -1;
    public int      mRunLoop = 0;
    public CPageTask   mTask;

    public boolean isLoopStart(){
        if(mCode == null || mCode.length<2)
            return false;
        return mCode[1] == 0x4d;
    }

    public boolean isLoopEnd(){
        if(mCode == null || mCode.length<2)
            return false;
        return mCode[1] == 0x4e;
    }

    public boolean setData(byte[] data){
        if(data == null || data.length < 11)
            return false;
        mDelay = data[0]*0x100+data[1];
        mJump = data[10];
        mCode = new byte[8];
        System.arraycopy(data,2,mCode,0,8);

        return true;
    }

    public static CPageTaskNode tasknodeCreateFromString(String data,CPageTask task)
    {
        if(data == null || data.length()<3)
            return null;
        String[] strings = data.split(",");
        if(strings == null || strings.length!=2)
            return null;
        CPageTaskNode node = new CPageTaskNode();

        node.mTask = task;
        node.mBus = Integer.parseInt(strings[0]);
        if(!node.setData(WSUtil.StringToByteArray(strings[1])))
           return null;
        return node;
    }
}
