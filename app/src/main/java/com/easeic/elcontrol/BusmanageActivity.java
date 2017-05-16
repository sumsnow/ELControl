package com.easeic.elcontrol;

import android.app.ActionBar;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import br.com.dina.ui.widget.UITableView;

public class BusmanageActivity extends Activity {

    int mClickIndex = -1;

    HashMap<CTransport,Boolean> dataMap;
    Handler mHandle = null;
    Boolean mRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        mRunning = true;
        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(false);
        setContentView(R.layout.activity_busmanage);
        final UITableView tableView = (UITableView)findViewById(R.id.tableView);
        dataMap = new HashMap<>();
        for (CTransport item :
                CProject.sProject.arraryTransports) {
            tableView.addItem(item.mName, item.isConnected() ? getString(R.string.transport_connect) : getString(R.string.transport_disconnect),item instanceof CTCPTransport);
            dataMap.put(item,Boolean.valueOf(item.isConnected()));
        }

        tableView.setClickListener(new UITableView.ClickListener() {
            @Override
            public void onClick(int index) {
                CTransport transport = CProject.sProject.arraryTransports.elementAt(index);
                if(transport == null || !(transport instanceof CTCPTransport))
                    return;
                final Dialog login = new Dialog(BusmanageActivity.this);

                // Set GUI of login screen
                login.setContentView(R.layout.login_dialog);
                login.setTitle(String.format(getString(R.string.transport_logintitle),((CTCPTransport) transport).mName));
                CTCPTransport tcp = (CTCPTransport)transport;

                // Init button of login GUI
                Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
                btnLogin.setText(R.string.transport_loginbtn);
                Button btnCancel = (Button) login.findViewById(R.id.btnCancel);
                final EditText txtUsername = (EditText)login.findViewById(R.id.txtUsername);
                final EditText txtPassword = (EditText)login.findViewById(R.id.txtPassword);

                txtUsername.setHint(R.string.transport_userhint);
                txtPassword.setHint(R.string.transport_pswhint);

                if(tcp.mUser != null)
                    txtUsername.setText(tcp.mUser);
                if(tcp.mPsw != null)
                    txtPassword.setText(tcp.mPsw);
                mClickIndex = index;
                // Attached listener for login GUI button
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(txtUsername.getText().toString().trim().length() > 0 && txtPassword.getText().toString().trim().length() > 0)
                        {
                            // Validate Your login credential here than display message
                           if(mClickIndex<0)
                               return;
                            CTCPTransport tcp = (CTCPTransport)CProject.sProject.arraryTransports.elementAt(mClickIndex);
                            // Redirect to dashboard / home screen.
                            tcp.login(txtUsername.getText().toString().trim(),txtPassword.getText().toString().trim());
                            CProject.sProject.saveTCPConf();
                            login.dismiss();
                        }
                        else
                        {
                            Toast.makeText(BusmanageActivity.this,getString(R.string.transport_errhint),Toast.LENGTH_LONG).show();
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        login.dismiss();
                    }
                });

                // Make dialog box visible.
                login.show();
            }
        });
        tableView.commit();

        mHandle = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                CTransport item = CProject.sProject.arraryTransports.elementAt(msg.what);
                tableView.editItem(msg.what,item.mName,item.isConnected() ? getString(R.string.transport_connect) : getString(R.string.transport_disconnect)
                        ,item.isConnected()? Color.BLACK:Color.RED);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                long tick = System.currentTimeMillis();
                while (!Thread.currentThread().isInterrupted() && mRunning) {
                    if ((System.currentTimeMillis() - tick) > 1000) {
                        int index = 0;

                        for (CTransport item :
                                CProject.sProject.arraryTransports) {
                            Boolean data = dataMap.get(item);
                            if(data != null && data.booleanValue() != item.isConnected()){
                                //find cell
                                data = item.isConnected();
                                Message msg = new Message();
                                msg.what = index;
                                mHandle.sendMessage(msg);
                            }
                            index++;
                        }
                    }
                    try{
                        Thread.sleep(10);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            onBackPressed();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mRunning = false;
        super.onDestroy();
    }
}
