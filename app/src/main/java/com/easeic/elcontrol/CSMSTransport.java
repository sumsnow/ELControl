package com.easeic.elcontrol;

import android.telephony.SmsManager;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/4/17.
 */
public class CSMSTransport extends CTransport {
    String  mNumber;
    @Override
    public boolean decoderString(String param) {
        mNumber = param;

        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public int send(byte[] data) {
        if(data == null || data.length<1)
            return 0;
        String context = WSUtil.ByteArrayToString(data);
        if(context == null || context.length()<1)
            return 0;
        SmsManager smsManager = SmsManager.getDefault();
        try{
            smsManager.sendTextMessage(mNumber,null,context,null,null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 1;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    final static String S_PARAM = "Param";
    public static CSMSTransport transportFromXML(Element xmlData, CProject project)
    {
        CSMSTransport transport = new CSMSTransport();
        transport.mProject = project;
        transport.mID = Integer.parseInt(xmlData.getAttribute(S_ID));
        transport.mName = xmlData.getAttribute(S_NAME);
        transport.mNote = xmlData.getAttribute(S_NOTE);
        transport.decoderString(xmlData.getAttribute(S_PARAM));

        return transport;
    }
}
