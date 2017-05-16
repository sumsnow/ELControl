package com.easeic.elcontrol;

/**
 * Created by sam on 2016/4/2.
 */
public class CFunctionBase extends CBase {
    public final static int functionNone = 0;
    public final static int functionEvent = 1;
    public final static int functionTask = 2;
    public final static int functionTiming = 3;

    public String   stringDetail;

    public int getFunctionClass(){
        return functionNone;
    }
}
