package com.easeic.elcontrol;

/**
 * Created by sam on 2016/3/24.
 */
public class CBase {
    public int mID = 0;
    public String mName;
    public CProject mProject;

    public static final String S_NAME = "Name";
    public static final String S_ID = "ID";
    public static final String S_TYPE = "Type";
    public static final String S_NOTE = "Note";

    public static byte calcCheck(byte[] buf,int len){
        byte ret = 0;
        for (int i=0;i<len;i++) {
            ret += buf[i];
        }

        return (byte)~ret;
    }
}
