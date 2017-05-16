package com.easeic.elcontrol;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sam on 2016/4/6.
 */
public class FileTransport extends AsyncTask<String,Integer,Integer> {
    public ServerSocket mServerSocket = null;
    Socket mClientSocket = null;

    FileOutputStream mDOS;
    DataInputStream mDIS;

    MainActivity    mActivity;
    int     mFileLength = 0;

    ProgressDialog  mDlg;


    public FileTransport(ProgressDialog dlg) {
        super();
        mDlg = dlg;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDlg.show();
      //  mDlg.findViewById(android.R.id.progress).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        if(mServerSocket != null)
        {
            try{
                mServerSocket.close();
            }
            catch (Exception e)
            {

            }
        }

        if(integer == 1)
        {
            mDlg.setMessage(WSUtil.loadString(R.string.upload_endupload));
          //  mDlg.findViewById(android.R.id.progress).setVisibility(View.INVISIBLE);
            Button btn = (Button)mDlg.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDlg.dismiss();
                    mActivity.mView.waitLoad();
                }
            },2000);
        }
        else if(integer == 0)
        {
            mDlg.setMessage(WSUtil.loadString(R.string.upload_failupload));
          //  mDlg.findViewById(android.R.id.progress).setVisibility(View.INVISIBLE);
            Button btn = (Button)mDlg.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn.setText(WSUtil.loadString(R.string.dialog_ok));
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if(values[0] == 0)
        {
            mDlg.setMessage(WSUtil.loadString(R.string.upload_startupload));
            mDlg.findViewById(android.R.id.progress).setVisibility(View.VISIBLE);
        }
        else
            mDlg.setProgress(values[0]);
    }

    @Override
    protected Integer doInBackground(String... params) {
        try{
            Log.i("AsyncTask", "doInBackgoung: Creating Socket");
            mServerSocket = new ServerSocket(2000);
          //  mServerSocket.setSoTimeout(5000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.i("AsyncTank", "doInBackgoung: Cannot create Socket");

            return -1;//init fail
        }
        try{
            mClientSocket = mServerSocket.accept();
            Log.d("AsyncTask", "client connected");
            mDIS = new DataInputStream(mClientSocket.getInputStream());
            mDOS = null;
            publishProgress(0);//0=connected
            long timeout = System.currentTimeMillis();
            byte[] buf = new byte[1024];
            int filePos = 0;
            while((System.currentTimeMillis()-timeout)<2000){
                int  len = mDIS.read(buf,0,1024);
                if(len>0){
                    timeout = System.currentTimeMillis();
                    if(mFileLength == 0 && mDOS == null){
                        mFileLength = WSUtil.byteArrayToInt(buf);
                        if(mFileLength<1) {
                            return 0;
                        }
                        //
                        File file = new File(params[0]);
                        mDOS = new FileOutputStream(file,false);
                        if(len > 4) {
                            mDOS.write(buf, 4, len - 4);
                            filePos = len - 4;
                            publishProgress((int) ((filePos / (float) mFileLength) * 100));

                            if(filePos>= mFileLength)
                            {
                                mDOS.close();
                                DataOutputStream dos = new DataOutputStream(mClientSocket.getOutputStream());
                                dos.write(0xff);
                                return 1;
                            }
                        }
                    }
                    else if(mFileLength>0 && mDOS != null){
                        filePos += len;
                        publishProgress((int) ((filePos / (float) mFileLength) * 100));
                        mDOS.write(buf,0,len);

                        if(filePos>= mFileLength)
                        {
                            mDOS.close();
                            DataOutputStream dos = new DataOutputStream(mClientSocket.getOutputStream());
                            dos.write(0xff);
                            return 1;
                        }
                        else
                        {
                            DataOutputStream dos = new DataOutputStream(mClientSocket.getOutputStream());
                            dos.write(0x01);
                        }
                    }
                    else
                        return 0;
                }
            }
        }
        catch (Exception e)
        {
            Log.i("AsyncTask","accept exception");
            e.printStackTrace();
        }

        return 0;
    }
}
