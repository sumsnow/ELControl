package com.easeic.elcontrol;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.io.File;
import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;
import com.github.danielnilsson9.colorpickerview.preference.ColorPreference;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

/**
 * Created by sam on 2016/4/8.
 */
public class ELConfFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
     DirectoryChooserFragment.OnFragmentInteractionListener
{

    static final int PREFERENCE_DIALOG_ID = 1;

    private DirectoryChooserFragment mDialog = null;
    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(WSUtil.loadString(R.string.pref_setting));
        addPreferencesFromResource(R.xml.elconf);

        // Find preference and add code to handle showing the ColorPickerDialogFragment
        // once requested.
        ColorPreference pref = (ColorPreference) findPreference(WSUtil.loadString(R.string.pref_colorback));
        if(pref != null)
        pref.setOnShowDialogListener(new ColorPreference.OnShowDialogListener() {

            @Override
            public void onShowColorPickerDialog(String title, int currentColor) {

                // Preference was clicked, we need to show the dialog.
                ColorPickerDialogFragment dialog = ColorPickerDialogFragment
                        .newInstance(PREFERENCE_DIALOG_ID, WSUtil.loadString(R.string.colorpicker_title), WSUtil.loadString(R.string.dialog_ok), currentColor, false);

                dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme);

                // PLEASE READ!
                // Show the dialog, the result from the dialog
                // will end up in the parent activity since
                // there really isn't any good way for fragments
                // to communicate with each other. The recommended
                // ways is for them to communicate through their
                // host activity, thats what we will do.
                // In our case, we must then make sure that MainActivity
                // implements ColorPickerDialogListener because that
                // is expected by ColorPickerDialogFragment.
                //
                // We also make this fragment implement ColorPickerDialogListener
                // and when we receive the result in the activity's
                // ColorPickerDialogListener when just forward them
                // to this fragment instead.
                dialog.show(getActivity().getFragmentManager(), "pre_dialog");
            }
        });
        Preference wherePref = (Preference)findPreference(WSUtil.loadString(R.string.pref_savewhere));
        if(wherePref != null)
        {
            wherePref.setSummary(ELConfigure.mSaveWhere);
            wherePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    File f = new File(ELConfigure.mSaveWhere+"/UEA");

                    DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                            .newDirectoryName("directory")
                            .initialDirectory(f.exists()?ELConfigure.mSaveWhere:"/")
                            .build();
                    mDialog = DirectoryChooserFragment.newInstance(config);
                    mDialog.setDirectoryChooserListener(ELConfFragment.this);
                    mDialog.show(getActivity().getFragmentManager(), null);

                    return true;
                }
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ELConfigure.load();

        if(key.compareTo(getString(R.string.pref_savewhere)) == 0)
        {
            Preference pref = (Preference)findPreference(key);
            if(pref != null)
                pref.setSummary(ELConfigure.mSaveWhere);
        }

    }

    @Override
    public void onSelectDirectory(String path) {
        Preference wherePref = (Preference)findPreference(WSUtil.loadString(R.string.pref_savewhere));
        if(wherePref != null) {
            wherePref.setSummary(path);
            if(ELConfigure.mSaveWhere.compareTo(path)!=0) {
                ELConfigure.mSaveWhere = path;
                ELConfigure.save();
                File f = new File(path+"/UEA");
                if(!f.exists()){
                    f.mkdir();
                }
            }
        }
        mDialog.dismiss();
        mDialog = null;
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
        mDialog = null;
    }
}
