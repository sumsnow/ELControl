package com.easeic.elcontrol;

import org.w3c.dom.Element;

/**
 * Created by sam on 2016/4/3.
 */
public class CPageTiming extends CFunctionBase {
    public int timeType;
    public int makeupTime;
    public int mPrior;
    public int mWeekday;
    public int mMonth;
    public int mMonthday;
    public int mFrequent;
    public int mcmdType;
    public int mCheckWeekday;
    public byte[]   mCode;
    public int  mBus;

    @Override
    public int getFunctionClass() {
        return functionTiming;
    }

    public static CPageTiming timingCreateFromXML(Element xmlData,CProject project)
    {
        CPageTiming timing = new CPageTiming();
        timing.mProject = project;
        timing.mName = xmlData.getAttribute(S_NAME);
        timing.mID = Integer.parseInt(xmlData.getAttribute(S_ID));
        timing.stringDetail = xmlData.getAttribute(S_NOTE);
        timing.loadData(xmlData.getTextContent());

        return timing;
    }

    public boolean isInvalid(){
        return mFrequent == 0;
    }

    public void loadData(String data){
        if(data == null || data.length()<1)
            return;
        String[] strings = data.split(";");
        timeType = Integer.parseInt(strings[0]);
        makeupTime = Integer.parseInt(strings[1]);
        mPrior = Integer.parseInt(strings[2]);
        mWeekday = Integer.parseInt(strings[3]);
        mMonth = Integer.parseInt(strings[4]);
        mMonthday = Integer.parseInt(strings[5]);
        mFrequent = Integer.parseInt(strings[6]);
        mCheckWeekday = Integer.parseInt(strings[7]);
        mBus = Integer.parseInt(strings[8]);
        mCode = WSUtil.StringToByteArray(strings[9]);
    }

}
