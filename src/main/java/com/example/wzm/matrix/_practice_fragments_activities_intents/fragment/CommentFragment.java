package com.example.wzm.matrix._practice_fragments_activities_intents.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix._practice_fragments_activities_intents.adapter.EventAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends Fragment {

    // Container Activity must implement this interface
    public interface OnItemSelectListener {
        public void onCommentSelected(int position);
    }
    OnItemSelectListener mCallback;
    private GridView mGridView;
    private int lastpos = -1;

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mGridView = (GridView) view.findViewById(R.id.comment_grid);
        mGridView.setAdapter(new EventAdapter(getActivity()));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onCommentSelected(i);
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

    // Change background color if the item is selected
    public void onItemSelected(int position){
        int count = mGridView.getChildCount();
        if(lastpos>=0 && lastpos<count)
            mGridView.getChildAt(lastpos).setBackgroundColor(Color.parseColor("#FAFAFA"));
        if(position<count)  {
            mGridView.getChildAt(position).setBackgroundColor(Color.BLUE);
            lastpos = position;
        }
    }
}
