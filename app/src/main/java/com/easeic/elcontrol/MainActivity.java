package com.easeic.elcontrol;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity implements Thread.UncaughtExceptionHandler{
    public SlidingMenu mMenu;
    TextView toolbarTitle;
    ITSView     mView;
    FileTransport   mFT;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // updatePosition();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("activity","ConfigureChanged");
        updatePosition();

/*        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && ELConfigure.mScreenDir == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && ELConfigure.mScreenDir == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);*/
/*        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePosition();
            }
        },30);*/
    }

    public void ungisteBackgroundAction(){
        if(Foreground.get().hasListen())
        {
            Foreground.get().removeAllListener();
        }
    }

    public void registeBackgroundAction(){
        if(!Foreground.get().hasListen()) {
            Foreground.get().addListener(new Foreground.Listener() {
                @Override
                public void onBecameForeground() {

                    stopService(new Intent(MainActivity.this,
                            BackgroundService.class));
                    //               Toast.makeText(MainActivity.this,"ELControl to foregournd",Toast.LENGTH_SHORT);
                }

                @Override
                public void onBecameBackground() {

                    startService(new Intent(MainActivity.this, BackgroundService.class));


//                Toast.makeText(MainActivity.this,"ELControl to bckground",Toast.LENGTH_SHORT);
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 2909: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");

                    //registeBackgroundAction();
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
        //Thread.setDefaultUncaughtExceptionHandler(this);


        if(WSUtil.wsRes == null) {
            WSUtil.wsRes = getResources();
            ELConfigure.load();
        }

        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      //  requestWindowFeature(Window.FEATURE_NO_TITLE);
       // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
/*        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/

/*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        //toolbar.setLogo(R.mipmap.option);
        toolbar.setNavigationIcon(R.mipmap.option);
        toolbar.setClickable(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenu.toggle();
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        ActionBar ab = getActionBar();
        //ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        toolbarTitle = new TextView(this);
        toolbarTitle.setGravity(Gravity.CENTER);
        ActionBar.LayoutParams layoutParams =new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0,0,60,0);
        ab.setCustomView(toolbarTitle, layoutParams);
        ab.setDisplayShowTitleEnabled(false);
        toolbarTitle.setText(R.string.app_name);
        toolbarTitle.setTextColor(Color.rgb(255,255,255));
        toolbarTitle.setTextSize(2,20);
        ab.setDisplayShowCustomEnabled(true);

        if(CProject.sProject != null)
            setTitle(CProject.sProject.mName);
        else
            setTitle(R.string.app_name);

        //ITSView
    /*    View oldView = findViewById(R.id.main_view);
        oldView.setBackgroundColor(ELConfigure.colorBackground);
        View tmp = oldView.findViewById(R.id.itsview);
        ViewGroup parent = (ViewGroup)oldView;
        parent.removeView(tmp);
        mView = new ITSView(this);
        parent.addView(mView);
*/
        mView = (ITSView)findViewById(R.id.itsview);
        mView.backView = (BackView)findViewById(R.id.backview);
        mView.backView.setVisibility(View.GONE);
        mView.mContainer =  findViewById(R.id.main_view);
        mView.mContainer.setBackgroundColor(ELConfigure.colorBackground);

        mMenu = new SlidingMenu(this);
        mMenu.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT,false);

        mMenu.setMode(SlidingMenu.LEFT);
        mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mMenu.setShadowWidthRes(R.dimen.menu_shadow_width);
        mMenu.setShadowDrawable(R.drawable.shadow_menu);
        mMenu.setBehindOffsetRes(R.dimen.menu_behind_offset);
        mMenu.setFadeDegree(0.35f);
        mMenu.setBehindWidth(400);
        try {
         //   mMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        }
        catch (Exception e)
        {

        }
        mMenu.setMenu(R.layout.fragment_menu);

        getFragmentManager().beginTransaction().replace(R.id.fragment_menu,
                new MenuFragment()).commit();

        if(CProject.sProject != null)
            updatePosition();

        registeBackgroundAction();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!Settings.System.canWrite(MainActivity.this)) {
                        MainActivity.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                    } else {
                        // continue with your code
                    }
                }
            },5000);
        }
/*        mView = new ITSView(this);
        mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(mView);*/
    }

    void updatePosition(){
        mView.requestLayout();
        if(CProject.sProject == null)
        {

        }
 /*       View parent =(View) mView.getParent();
        if(mView.mProject == null || mView.mLoading )
        {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(0,0);
            mView.setLayoutParams(layoutParams);

        }
        else
        {
            int left = 0;
            int top = 0;
            int x = parent.getWidth();
            int y = parent.getHeight();

            Point pt = mView.mProject.getSize();
            if(pt.x <= 0)
                pt.x = 1;
            if(pt.y <= 0)
                pt.y = 1;

            double xs = x * 1.0 / pt.x;
            double ys = y * 1.0 / pt.y;

            mView.mScale = xs < ys ? xs : ys;

            int nx = (int) (pt.x * mView.mScale);
            int ny = (int) (pt.y * mView.mScale);

            left = (x - nx) / 2;
            top = (y - ny) / 2;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(left, top, left,top);
            mView.setLayoutParams(layoutParams);
            String str = String.format("%d,%d,%d,%d,%d,%d",x,y,left,top,nx,ny);
            Log.i("activity",str);
        }*/
    }

    Boolean isService = true;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
  //      getMenuInflater().inflate(R.menu.menu_main, menu);
  //      return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isService) {
/*            startService(new Intent(this, BackgroundService.class));
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
  */      }

        isService = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    /*    stopService(new Intent(this,
                BackgroundService.class));*/
        if(isService)
        {
        }

        ELConfigure.load();
        if(ELConfigure.mScreenDir == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if(ELConfigure.mScreenDir == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if(!ELConfigure.mShowStatus && CProject.sProject != null)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //titlebar
        ActionBar ab = getActionBar();
        if(ab != null && (ELConfigure.mShowNav  || CProject.sProject == null)) {
            mMenu.setFitsSystemWindows(true);
            ab.show();
        }
        else if(ab!=null) {
            mMenu.setFitsSystemWindows(false);
            ab.hide();
        }

        if(mView != null && mView.mContainer != null)
                mView.mContainer.setBackgroundColor(ELConfigure.colorBackground);
        updatePosition();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (toolbarTitle != null) {

           toolbarTitle.setText(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == android.R.id.home)
            mMenu.toggle();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        registeBackgroundAction();
    }

    //
    public void onMenuSelected(int index){

        mMenu.toggle();
        Log.i("menu", Integer.toString(index));
        switch (index)
        {
            case 0:
            {

                isService = false;
                Intent  intent = new Intent(this, ELConfActivity.class);
                //startActivity(intent);
                ungisteBackgroundAction();
                startActivityForResult(intent,0);
               // registeBackgroundAction();
            }
            break;
            case 1:
            {
                mView.waitLoad();
            }
            break;
            case 2:
            {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.option_delete)
                        .setMessage(R.string.delete_hint)
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteDirectoryOrFile(new File(ELConfigure.mSaveWhere+"/UEA/Project"));
                                                              ;
                                mView.waitLoad();                                                         ;
                            }
                        }).show();
            }
            break;
            case 3://upload
            {
                StringList ips = IPHelp.getIPAddress(true);
                if(ips == null || ips.getCount()<1) {
                    Toast.makeText(this, R.string.upload_noip, Toast.LENGTH_LONG).show();
                    break;
                }
                if(ips.getCount()>1)
                {
                    final String[] strings = new String[ips.getCount()];
                    for (int i=0;i<ips.getCount();i++){
                        strings[i] = ips.getAt(i);
                    }
                    if(mWhichIP>=ips.getCount())
                        mWhichIP = 0;
                    new AlertDialog.Builder(this).setTitle(R.string.upload_selectip)
                            .setSingleChoiceItems(strings, mWhichIP, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mWhichIP = which;
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel,null)
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    uploadProject(strings[mWhichIP]);
                                }
                            })
                            .show();
                    break;
                }
                uploadProject(ips.getAt(0));

            }
            break;
            case 4:
            {
                if(CProject.sProject == null)
                    return;

                isService = false;
                Intent  intent = new Intent(this, BusmanageActivity.class);
              //  startActivity(intent);

                ungisteBackgroundAction();
                startActivityForResult(intent,1);
            }
            break;
            case 5:
            {
                if(CProject.sProject == null){
                   Toast.makeText(this,R.string.homepage_noproject,Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.option_homepage)
                        .setMessage(R.string.homepage_hint)
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                             ELConfigure.mHomepage = CProject.sProject.getActivePage().mID;
                                ELConfigure.mProjectName = CProject.sProject.mName;
                                ELConfigure.save();
                            }
                        }).show();

            }
            break;
            case 6:
            {
                new AboutDialog(this).show();
            }
            break;
        }
    }

    int mWhichIP = 0;

    void uploadProject(String ip){

        String title = WSUtil.loadString(R.string.option_upload);
        title += "("+ ip + ")";

        ProgressDialog progDlg = new ProgressDialog(this);
        progDlg.setTitle(title);
        progDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progDlg.setCancelable(false);
        progDlg.setMax(100);
        progDlg.setButton(DialogInterface.BUTTON_NEGATIVE, WSUtil.loadString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mFT.mServerSocket.close();
                }
                catch (Exception e)
                {

                }
                dialog.cancel();
            }
        });
        progDlg.setMessage(WSUtil.loadString(R.string.upload_wait));

        mFT = new FileTransport(progDlg);
        mFT.mActivity = this;
        File f = new File(ELConfigure.mSaveWhere+"/UEA");
        if(!f.exists())
            if(!f.mkdir())
            {
                String info = String.format("Not exist the directory(%s)!",f.getPath());
                Toast.makeText(this,info,Toast.LENGTH_SHORT).show();
                return;
            }
        mFT.execute(ELConfigure.mSaveWhere+"/UEA/project.zea");
    }


    public static void deleteDirectoryOrFile(File file){
        if(file.isDirectory())
        {
            File[] lists = file.listFiles();
            for (File child:lists)
                deleteDirectoryOrFile(child);
        }
        file.delete();
    }

    int mKeyExitCount = 0;
    long mKeyTick = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mKeyExitCount>0 && (System.currentTimeMillis()-mKeyTick)<3000)
            {
                finish();
                System.exit(0);
            }
            else {
                mKeyExitCount = 1;
                mKeyTick = System.currentTimeMillis();
                Toast toast = Toast.makeText(this,R.string.exit_hint,Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Log.i("AAA", "uncaughtException   " + ex);
    }
}
