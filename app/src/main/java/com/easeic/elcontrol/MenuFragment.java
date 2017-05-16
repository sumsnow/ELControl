package com.easeic.elcontrol;


import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link ListFragment} subclass.
 */
public class MenuFragment extends ListFragment {


    private ListView list ;
    private SimpleAdapter adapter;

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        if(view != null) {
            list = (ListView) view.findViewById(android.R.id.list);
         /*   TextView text = (TextView)view.findViewById(R.id.menu_title);
            if(text != null)
                text.setText(R.string.option_title);*/
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int[] list = {R.string.option_params,R.string.option_reload,R.string.option_delete,R.string.option_upload,R.string.title_activity_busmanage,R.string.option_homepage,R.string.about_title};
        //String[] list = {"Class 1","Class 2","class 3","Class 4","Class 5"};
        adapter = new SimpleAdapter(getActivity(), getData(list), R.layout.menu_item, new String[]{"option_set"}, new int[]{R.id.option_set});
        setListAdapter(adapter);
    }

    private List<? extends Map<String, ?>> getData(String[] strs) {
        List<Map<String ,Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < strs.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("option_set", strs[i]);
            list.add(map);

        }

        return list;
    }
    private List<? extends Map<String, ?>> getData(int[] strs) {
        List<Map<String ,Object>> list = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < strs.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("option_set", getString(strs[i]));
            list.add(map);

        }

        return list;
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        /*System.out.println(l.getChildAt(position));
        HashMap<String, Object> view= (HashMap<String, Object>) l.getItemAtPosition(position);
        System.out.println(view.get("title").toString()+"+++++++++title");
        Toast.makeText(getActivity(), TAG+l.getItemIdAtPosition(position), Toast.LENGTH_LONG).show();
        System.out.println(v);

        System.out.println(position);

        */

        MainActivity activity = (MainActivity)getActivity();
        if(activity != null)
            activity.onMenuSelected(position);
    }

}
