package com.easeic.elcontrol;

/**
 * Created by sam on 2016/4/2.
 */
public class CPageEventNode {
    public byte[]   mCode;
    public int      mDelay = 0;
    public int      mBus = 0;
    public CPageEvent   mEvent;

    public boolean setData(byte[] data){
        if(data == null || data.length < 10)
            return false;
        mDelay = data[0]*0x100+data[1];
        mCode = new byte[8];
        System.arraycopy(data,2,mCode,0,8);

        return true;
    }

    public static CPageEventNode eventnodeCreateFromString(String data,CPageEvent event)
    {
        if(data == null || data.length()<3)
            return null;
        String[] strings = data.split(",");
        if(strings == null || strings.length!=2)
            return null;
        CPageEventNode node = new CPageEventNode();

        node.mEvent = event;
        node.mBus = Integer.parseInt(strings[0]);
        if(!node.setData(WSUtil.StringToByteArray(strings[1])))
            return null;
        return node;
    }
}
