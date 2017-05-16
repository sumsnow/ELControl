package com.easeic.elcontrol;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/3/25.
 */
public class CUIFactory {
    final static String S_CLASS = "Class";
    public static CUIBase createUIFromXML(Element xmlData,CUIBase page,CProject project)
    {
        String tmp = xmlData.getAttribute(S_CLASS);
        if(tmp == null)
            return null;
        switch(Integer.parseInt(tmp))
        {
            case CUIBase.UICLASS_SHAPE:
            {
                CUIShape item = new CUIShape();
                item.mProject = project;
                item.mOwner = page;

                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_BUTTON:
            {
                CUIButton item = new CUIButton();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_LOAD:
            {
                CUILoad item = new CUILoad();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_CLOCK:
            {
                CUIClock item = new CUIClock();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_FEEDBACK:
            {
                CUIFeedback item = new CUIFeedback();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_TEXT:
            {
                CUIText item = new CUIText();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_IMAGE:
            {
                CUIImage item = new CUIImage();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_SLIDER:
            {
                CUISlider item = new CUISlider();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
            case CUIBase.UICLASS_SUBPAGE:
            {
                CSubPage item = new CSubPage();
                item.mProject = project;
                item.mOwner = page;
                item.loadXML(xmlData);
                return item;
            }
        }

        return null;
    }
}
