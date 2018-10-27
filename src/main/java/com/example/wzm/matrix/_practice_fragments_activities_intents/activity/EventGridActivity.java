package com.example.wzm.matrix._practice_fragments_activities_intents.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix._practice_fragments_activities_intents.fragment.CommentFragment;

public class EventGridActivity extends AppCompatActivity  implements CommentFragment.OnItemSelectListener {
    int pos = 0;
    CommentFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_grid);
        Intent intent = getIntent();
        pos = intent.getIntExtra("position", 0);
        fragment = new CommentFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.grid_container, fragment).commit();
    }

    @Override
    public void onCommentSelected(int position) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) fragment.onItemSelected(pos);
    }

}
