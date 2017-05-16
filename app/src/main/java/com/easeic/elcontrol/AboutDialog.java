package com.easeic.elcontrol;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutDialog extends AlertDialog {

    private ImageView mIconView;
    private TextView mAppNameText;
    private TextView			mAboutText;
    private TextView			mVersionText;


    public AboutDialog(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_about, null);

        mAboutText = (TextView) layout.findViewById(android.R.id.text2);
        mVersionText = (TextView) layout.findViewById(android.R.id.text1);
        mAppNameText = (TextView) layout.findViewById(android.R.id.title);
        mIconView = (ImageView) layout.findViewById(android.R.id.icon);

        setView(layout);

        loadAbout();

        setTitle(R.string.about_title);



        mIconView.setOnClickListener(new View.OnClickListener() {

            int mClickCount = 0;

            @Override
            public void onClick(View v) {
                mClickCount++;

                if(mClickCount == 5) {
                    mClickCount = 0;
                    WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    Toast.makeText(getContext(),String.format("Screen Resulotion: %dX%d",size.x,size.y) , Toast.LENGTH_SHORT).show();

                }

            }
        });



        setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }

    private void loadAbout(){

        PackageInfo pi = null;
        try {
            pi = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mAppNameText.setText(getContext().getString(R.string.app_name));
        mVersionText.setText("Version" + " " + (pi != null ? pi.versionName : "null"));

        String s = "<b>Developed By:</b><br>Easeic<br>";
        mAboutText.setText(Html.fromHtml(s));

    }

}
