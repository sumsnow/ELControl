package com.easeic.elcontrol;

import android.util.Log;

import org.w3c.dom.Element;

import java.util.Vector;

/**
 * Created by sam on 2016/4/2.
 */

public class CPageEvent extends CFunctionBase{
    public Vector<CPageEventNode> arrayCodes;

    public void clearAll(){
        if(arrayCodes != null)
            arrayCodes.clear();
    }

    public void loadData(String data){
        if(data == null || data.length()<1)
            return;
        String[] strings = data.split(";");

        for (String item:strings)
        {
            CPageEventNode node = CPageEventNode.eventnodeCreateFromString(item,this);
            if(node != null) {
                if (arrayCodes == null)
                    arrayCodes = new Vector<CPageEventNode>();
                arrayCodes.add(node);
            }
        }
    }

    @Override
    public int getFunctionClass() {
        return functionEvent;
    }

    public static CPageEvent eventCreateFromXML(Element xmlData,CProject project)
    {
        CPageEvent event = new CPageEvent();
        event.mProject = project;
        event.mName = xmlData.getAttribute(S_NAME);
        event.mID = Integer.parseInt(xmlData.getAttribute(S_ID));
        event.stringDetail = xmlData.getAttribute(S_NOTE);
        String str = xmlData.getTextContent();
        event.loadData(xmlData.getTextContent());

        return event;
    }
}
