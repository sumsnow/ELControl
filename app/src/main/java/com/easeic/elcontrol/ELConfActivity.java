package com.easeic.elcontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;
import com.github.danielnilsson9.colorpickerview.preference.ColorPreference;

public class ELConfActivity extends Activity  implements ColorPickerDialogFragment.ColorPickerDialogListener {

    TextView toolbarTitle;
    ELConfFragment mELConfFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(false);
        setContentView(R.layout.activity_elconf);
        //toolbar.setLogo(R.mipmap.option);
        mELConfFragment = new ELConfFragment();
        getFragmentManager().beginTransaction().replace(R.id.conf_view,
                mELConfFragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {

        ColorPreference pref = (ColorPreference) mELConfFragment.findPreference(WSUtil.loadString(R.string.pref_colorback));
        pref.saveValue(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }
}
