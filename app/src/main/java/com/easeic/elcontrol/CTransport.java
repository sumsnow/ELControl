package com.easeic.elcontrol;

/**
 * Created by sam on 2016/4/3.
 */
public class CTransport extends CBase {
    public String mNote;

    public int send(byte[] data){return 0;}
    public byte[] recv(){return null;}
    public void flush(){}
    public boolean isConnected(){return false;}
    public boolean isRunning(){return false;}
    public void start(){}
    public void stop(){}
    public boolean decoderString(String param){return false;}
    public void editUI(CUIBase ui){}
}
