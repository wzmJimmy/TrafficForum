package com.example.wzm.matrix._practice_fragments_activities_intents.activity;


//import android.util.Log;
//import android.widget.ArrayAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix._practice_fragments_activities_intents.fragment.CommentFragment;
import com.example.wzm.matrix._practice_fragments_activities_intents.fragment.EventFragment;

public class MainActivity extends AppCompatActivity implements EventFragment.OnItemSelectListener,CommentFragment.OnItemSelectListener {

    private EventFragment mListFragment;
    private CommentFragment mGridFragment;
    int pos = -1;

    @Override
    public void onItemSelected(int position){
        if (!isTablet()) {
            Intent intent = new Intent(this, EventGridActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else {
            mGridFragment.onItemSelected(position);
        }
    }

    @Override
    public void onCommentSelected(int position) {
        mListFragment.onItemSelected(position);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(pos>0 && hasFocus) mListFragment.onItemSelected(pos);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        pos = intent.getIntExtra("position", -1);

        //add list view
        mListFragment = new EventFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.event_container, mListFragment).commit();


        //add Gridview
        if (isTablet()) {
            mGridFragment = new CommentFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.comment_container, mGridFragment).commit();
        }else{
            RelativeLayout layout = findViewById(R.id.event_container);
            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
