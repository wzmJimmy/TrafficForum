package com.example.wzm.matrix._practice_fragments_activities_intents.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix._practice_fragments_activities_intents.adapter.EventAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {

    // Container Activity must implement this interface
    public interface OnItemSelectListener {
        public void onItemSelected(int position);
    }

    OnItemSelectListener mCallback;
    private ListView listView;
    private int lastpos=-1;

    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        listView = (ListView) view.findViewById(R.id.event_list);
        listView.setAdapter(new EventAdapter(getActivity()));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                getEventNames());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onItemSelected(i);
                onItemSelected(i);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelectListener) context;
        } catch (ClassCastException e) {
            //do something
        }
    }

    public void onItemSelected(int position){
        int count = listView.getChildCount();
        if(lastpos>=0 && lastpos<count) listView.getChildAt(lastpos).setBackgroundColor(Color.parseColor("#FAFAFA"));
        if(position<count)  {
            listView.getChildAt(position).setBackgroundColor(Color.BLUE);
            lastpos = position;
        }
    }
    private String[] getEventNames() {
        String[] names = {
                "Event1", "Event2", "Event3",
                "Event4", "Event5", "Event6",
                "Event7", "Event8", "Event9",
                "Event10",};
        return names;
    }
}
