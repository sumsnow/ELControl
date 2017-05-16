package com.easeic.elcontrol;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static android.view.ViewGroup.*;

/**
 * Created by sam on 2016/4/9.
 */
public class CustomSwitchPreference extends SwitchPreference {

    public CustomSwitchPreference(final Context context, final AttributeSet attrs,
                                    final int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomSwitchPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchPreference(final Context context) {
        super(context);
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent) {
        final View v = super.getView(convertView, parent);
        final int hieght = 120;
        final int width = LayoutParams.MATCH_PARENT;
        final LayoutParams params = new LayoutParams(width, hieght);
        v.setLayoutParams(params );

        return v;
    }
}

