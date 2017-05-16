package com.easeic.elcontrol;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    static final int REPOSITION_VIEW = 223;
    static final int FLESH_VIEW = 224;
    boolean mLoading = false;
    Handler mHandle = null;
    TextView mText = null;
    int mDot = 0;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 2909: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");
                } else {
                    Log.e("Permission", "Denied");
                    Toast.makeText(this,"Can not be granted the permission of read and write. ",Toast.LENGTH_SHORT);
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
            } else {
                // continue with your code
            }
        }

        WSUtil.wsRes = getResources();
        ELConfigure.load();
        setContentView(R.layout.activity_splash);

        mText = (TextView) findViewById(R.id.splash_text);

        mHandle = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REPOSITION_VIEW:
                    {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    break;
                    case FLESH_VIEW:
                    {
                        String text = getResources().getString(R.string.wait_hint_nodot);
                        for (int i=0;i<mDot;i++)
                            text += ".";
                        mDot++;
                        if(mDot>6)
                            mDot = 0;
                        mText.setText(text);
                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(!Thread.currentThread().isInterrupted()){

                    if(CProject.sProject != null)
                    {
                        break;
                    }

                    Message msg = new Message();
                    msg.what = FLESH_VIEW;
                    mHandle.sendMessage(msg);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                mLoading = true;
                reload();
                mLoading = false;

                try {
                    Thread.sleep(2000);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.what = REPOSITION_VIEW;
                mHandle.sendMessage(msg);
            }
        }).start();
    }
    public void reload() {
        CProject.createProject();
    }

}
