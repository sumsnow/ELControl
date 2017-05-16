package com.easeic.elcontrol;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sam on 2016/4/4.
 */
public class ELConfigure {
    public static boolean mMute = false;
    public static boolean mCanAnimation = false;
    public static int   mAnimationType = 0;
    public static boolean mShowStatus = true;
    public static boolean mShowNav = true;
    public static int   mScreenDir = 0;//=0,any;1=portrait;2=land
    public static String    mSaveWhere;
    public static int   colorBackground;
    public static int mHomepage = 0;
    public static String mProjectName;
    public static HashMap<String,String> mTCPConfMap;

    public static boolean mFirstRun = true;

    static int SETTING = R.string.pref_setting;
    static int MUTE = R.string.pref_mute;
    static int SAVEWHERE = R.string.pref_savewhere;
    static int CANANIMATION = R.string.pref_cananimation;
    static String ANIMATIONTYPE = "AnimationType";
    static int SHOWSTATUS = R.string.pref_status;
    static int SHOWNAV = R.string.pref_nav;
    static int COLORBACK = R.string.pref_colorback;
    static int SCREENDIR = R.string.pref_screendir;
    static String TCPCONF = "TCPConf";
    static String HOMEPAGE = "HomePage";
    static String PROJECTFORHOMEPAGE = "ProjectForHomepage";
    static String FirstRun = "FirstRun";

    public static void load(){
        Resources res =  MyApplication.getAppContext().getResources();
        SharedPreferences settings = MyApplication.getAppContext().getSharedPreferences(res.getString(SETTING), 0);

        mMute = settings.getBoolean(res.getString(MUTE), false);
        mAnimationType = settings.getInt(ANIMATIONTYPE, 0);
        mCanAnimation = settings.getBoolean(res.getString(CANANIMATION), true);
        colorBackground = settings.getInt(res.getString(COLORBACK), 0xFFa0a0a0);
        mShowStatus = settings.getBoolean(res.getString(SHOWSTATUS), false);
        mShowNav = settings.getBoolean(res.getString(SHOWNAV), false);
        mScreenDir = Integer.parseInt(settings.getString(res.getString(SCREENDIR),"0"));
        mFirstRun = settings.getBoolean(FirstRun,true);
        if(mTCPConfMap == null){
            mTCPConfMap = new HashMap<>();
        }
        try{
                String jsonString = settings.getString(TCPCONF, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    mTCPConfMap.put(key, value);
                }

        }catch(Exception e){
            e.printStackTrace();
        }
        mHomepage = settings.getInt(HOMEPAGE,0);
        mProjectName = settings.getString(PROJECTFORHOMEPAGE,"");

        mSaveWhere = settings.getString(res.getString(SAVEWHERE), "/storage/emulated/0");
        if(mSaveWhere != null && mSaveWhere.length()>0 && mSaveWhere.charAt(0) != '/')
            mSaveWhere = "/" + mSaveWhere;
        if(mSaveWhere == null || mSaveWhere.length()<1 || !new File(mSaveWhere).canWrite())
        {
/*            String secondary_sd = System.getenv("SECONDARY_STORAGE");
            if(secondary_sd != null && new File(secondary_sd).canWrite()){
                Log.i("SECONDARY_STORAGE", secondary_sd);
                mSaveWhere = secondary_sd;
            }
            else*/
            {
                String primary_sd = System.getenv("EXTERNAL_STORAGE");
                if(primary_sd != null){
                    Log.i("EXTERNAL_STORAGE", primary_sd);
                    mSaveWhere = primary_sd;

                }
            }
   //         if(mSaveWhere.length()>0 && mSaveWhere.charAt(0) =='/')
    //           mSaveWhere = mSaveWhere.substring(1);


            File f = new File(ELConfigure.mSaveWhere+File.separator+ "UEA");
            if(!f.exists()) {
                boolean suc = f.mkdir();
                if (!suc)
                    Log.i("mainactivity", "create uea fail");
            }

        }
    }

    public static void save() {
        Resources res =  MyApplication.getAppContext().getResources();
        SharedPreferences settings = MyApplication.getAppContext().getSharedPreferences(res.getString(SETTING), 0);

        if(mTCPConfMap == null)
            mTCPConfMap = new HashMap<>();

        JSONObject jsonObject = new JSONObject(mTCPConfMap);
        String jsonString = jsonObject.toString();

        settings.edit().putBoolean(res.getString(MUTE), mMute)
                .putBoolean(res.getString(CANANIMATION), mCanAnimation)
                .putString(res.getString(SAVEWHERE), mSaveWhere)
                .putInt(ANIMATIONTYPE, mAnimationType)
                .putInt(res.getString(COLORBACK), colorBackground)
                        .putBoolean(res.getString(SHOWSTATUS), mShowStatus)
                                .putBoolean(res.getString(SHOWNAV), mShowNav)
                                        .putString(res.getString(SCREENDIR), Integer.valueOf(mScreenDir).toString())
                                            .putString(TCPCONF,jsonString)
                                                .putInt(HOMEPAGE,mHomepage)
                                                .putString(PROJECTFORHOMEPAGE,mProjectName)
                                                .putBoolean(FirstRun,mFirstRun)
                                                .commit();
    }
}
