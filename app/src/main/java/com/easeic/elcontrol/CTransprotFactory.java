package com.easeic.elcontrol;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/4/3.
 */
public class CTransprotFactory {
    final static String S_TYPE = "Type";

    public static CTransport transportCreateFromXML(Element xmlData,CProject project)
    {
        CTCPTransport ret = null;
        switch (Integer.parseInt(xmlData.getAttribute(S_TYPE)))
        {
            case 1:
            {
                return CTCPTransport.transportFromXML(xmlData,project);
            }
            case 2:
                return CSMSTransport.transportFromXML(xmlData,project);
        }

        return null;
    }
}
