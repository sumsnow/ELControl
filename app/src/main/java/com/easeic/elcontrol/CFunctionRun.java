package com.easeic.elcontrol;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by sam on 2016/4/5.
 */
public class CFunctionRun {

    public CFunctionBase funBase;
    public boolean  mRun = false;

    int mIndex = 0;
    long    runTick = 0;
    int mTmpParam = -2;
    CProject mProject = null;

    public CFunctionRun(CFunctionBase base,boolean run)
    {
        funBase = base;
        mRun = run;
        mIndex = 0;
        mProject = base.mProject;

    }

    public int execute(){
        if(funBase == null || mProject == null || !mRun)
            return 0;
        if(runTick == 0)
            runTick = System.currentTimeMillis();
        switch (funBase.getFunctionClass())
        {
            case CFunctionBase.functionEvent:
            {
                CPageEvent event = (CPageEvent)funBase;

                if(event.arrayCodes == null ||  mIndex >= event.arrayCodes.size())
                    return 0;
                CPageEventNode node = event.arrayCodes.elementAt(mIndex);
                if((System.currentTimeMillis()-runTick)<node.mDelay*100)
                    return 1;
                mProject.uiSend(node.mBus,node.mCode);
                runTick = System.currentTimeMillis();
                mIndex++;
                if(mIndex>=event.arrayCodes.size())
                    return 0;
                else
                    return 1;
            }
            case CFunctionBase.functionTask:
            {
                CPageTask task = (CPageTask)funBase;
                if(task.arrayCodes == null || mIndex>=task.arrayCodes.size())
                    return 0;
                CPageTaskNode node = task.arrayCodes.elementAt(mIndex);
                if((System.currentTimeMillis()-runTick)<node.mDelay*100)
                    return 1;
                runTick = System.currentTimeMillis();
                if(node.isLoopStart())
                {
                    if(mTmpParam == -2)
                        mTmpParam = node.mJump;
                    mTmpParam--;
                    mIndex++;
                    return 1;
                }
                else if(node.isLoopEnd())
                {
                    int nStart = task.findLoopStartFrom(mIndex);
                    if(mTmpParam == 0){
                        mIndex++;
                    if(mIndex>=task.arrayCodes.size())
                        return 0;
                    else
                    {
                        if(task.arrayCodes.elementAt(mIndex).isLoopStart())
                            mTmpParam = task.arrayCodes.elementAt(mIndex).mJump;
                        return 1;
                    }
                    }
                    else
                    {
                        mIndex = nStart;
                        return 1;
                    }
                }//end loopend
                else
                {
                    if(node.mCode != null && node.mCode.length>0)
                    {
                        if(node.mCode[1] != 0)
                            mProject.uiSend(node.mBus,node.mCode);
                    }

                    if(node.mJump>0)
                        mIndex = node.mJump-1;
                    else
                        mIndex++;
                    if(mIndex>=task.arrayCodes.size())
                        return 0;
                    else{
                        if(task.arrayCodes.elementAt(mIndex).isLoopStart())
                            mTmpParam = task.arrayCodes.elementAt(mIndex).mJump;
                        return 1;
                    }
                }
            }
            case CFunctionBase.functionTiming:
            {
                CPageTiming timing = (CPageTiming)funBase;
                if(timing.isInvalid())
                    return 0;
                Date sunDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(sunDate);

                switch (timing.mFrequent)
                {
                    case 1:
                    case 2:
                    {
                        if(timing.mMonth < cal.get(Calendar.MONTH)+1)
                            return 0;
                        if(timing.mMonth > cal.get(Calendar.MONTH)+1)
                            return 1;
                        if(timing.mMonthday < cal.get(Calendar.DAY_OF_MONTH))
                            return 0;
                        if(timing.mMonthday > cal.get(Calendar.DAY_OF_MONTH))
                            return 1;
                        int hour = timing.makeupTime/60;
                        int min = timing.makeupTime%60;
                        if(timing.timeType == 1)
                        {
                            int sunTime = CUIClock.getSunriseTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }
                        else if(timing.timeType == 2)
                        {
                            int sunTime = CUIClock.getSunsetTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }

                        if(hour < cal.get(Calendar.HOUR_OF_DAY))
                            return 0;
                        if(hour>cal.get(Calendar.HOUR_OF_DAY))
                            return 1;
                        if(min < cal.get(Calendar.MINUTE))
                            return 0;
                        if(min > cal.get(Calendar.MINUTE))
                            return 1;

                        if(timing.mCode != null && timing.mCode.length>0)
                            mProject.uiSend(timing.mBus,timing.mCode);
                        break;
                    }//end case 1,2
                    case 3:
                    {
                        int weekday = cal.get(Calendar.DAY_OF_WEEK);

                        if(weekday == 1 && (timing.mWeekday & (0x01 << 6)) == 0)
                        {
                            mTmpParam = -2;
                            return 1;
                        }
                        if((timing.mWeekday & (0x01 << (weekday-1))) == 0)
                        {
                            mTmpParam = -2;
                            return 1;
                        }

                        int hour = timing.makeupTime/60;
                        int min = timing.makeupTime%60;
                        if(timing.timeType == 1)
                        {
                            int sunTime = CUIClock.getSunriseTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }
                        else if(timing.timeType == 2)
                        {
                            int sunTime = CUIClock.getSunsetTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }

                        if(hour < cal.get(Calendar.HOUR_OF_DAY)) {
                            mTmpParam = -2;
                            return 1;
                        }
                        if(hour>cal.get(Calendar.HOUR_OF_DAY)) {
                            mTmpParam = -2;

                            return 1;
                        }
                        if(min < cal.get(Calendar.MINUTE)) {
                            mTmpParam = -2;
                            return 1;
                        }
                        if(min > cal.get(Calendar.MINUTE)) {
                            mTmpParam = -2;
                            return 1;
                        }

                        if(timing.mCode != null && timing.mCode.length>0) {
                            mTmpParam = 0;
                            mProject.uiSend(timing.mBus, timing.mCode);
                        }
                        break;
                    }//end case 3
                    case 4:
                    {
                        int hour = timing.makeupTime/60;
                        int min = timing.makeupTime%60;
                        if(timing.timeType == 1)
                        {
                            int sunTime = CUIClock.getSunriseTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }
                        else if(timing.timeType == 2)
                        {
                            int sunTime = CUIClock.getSunsetTiem(mProject);
                            int nTime = timing.makeupTime + sunTime;
                            hour = nTime/60;
                            min = nTime%60;
                        }

                        if(hour < cal.get(Calendar.HOUR_OF_DAY)) {
                            mTmpParam = -2;
                            return 1;
                        }
                        if(hour>cal.get(Calendar.HOUR_OF_DAY)) {
                            mTmpParam = -2;

                            return 1;
                        }
                        if(min < cal.get(Calendar.MINUTE)) {
                            mTmpParam = -2;
                            return 1;
                        }
                        if(min > cal.get(Calendar.MINUTE)) {
                            mTmpParam = -2;
                            return 1;
                        }

                        if(timing.mCode != null && timing.mCode.length>0) {
                            mTmpParam = 0;
                            mProject.uiSend(timing.mBus, timing.mCode);
                        }
                        break;
                    }//end case 4
                }//end switch frequent
            }
        }//end switch functionbase

        return 0;
    }
}
