package com.easeic.elcontrol;

import org.w3c.dom.Element;

import java.util.Vector;

/**
 * Created by sam on 2016/4/2.
 */
public class CPageTask extends CFunctionBase {
    public Vector<CPageTaskNode> arrayCodes;

    public void clearAll(){
        if(arrayCodes != null)
        arrayCodes.clear();
    }

    public int findLoopStartFrom(int index){
        int loop = 1;
        for (int i=index - 1;i>=0;i--)
        {
            CPageTaskNode node = arrayCodes.elementAt(i);
            if(node == null)
                continue;
            if(node.isLoopEnd())
                loop++;
            else if(node.isLoopStart())
                loop--;
            if(loop == 0)
                return i;
        }

        return -1;
    }

    public void loadData(String data){
        if(data == null || data.length()<1)
            return;
        String[] strings = data.split(";");

        for (String item:strings)
        {
            CPageTaskNode node = CPageTaskNode.tasknodeCreateFromString(item,this);
            if(node != null) {
                if(arrayCodes == null)
                    arrayCodes = new Vector<CPageTaskNode>();
                arrayCodes.add(node);
            }
        }
    }

    @Override
    public int getFunctionClass() {
        return functionTask;
    }

    public static CPageTask taskCreateFromXML(Element xmlData,CProject project)
    {
        CPageTask event = new CPageTask();
        event.mProject = project;
        event.mName = xmlData.getAttribute(S_NAME);
        event.mID = Integer.parseInt(xmlData.getAttribute(S_ID));
        event.stringDetail = xmlData.getAttribute(S_NOTE);
        event.loadData(xmlData.getTextContent());

        return event;
    }
}
