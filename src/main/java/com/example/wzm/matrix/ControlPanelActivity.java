package com.example.wzm.matrix;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wzm.matrix.adapter.MyPagerAdapter;
import com.example.wzm.matrix.config.Config;
import com.example.wzm.matrix.model.RealtimeLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;


public class ControlPanelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final static String TAG = ControlPanelActivity.class.getSimpleName()+"#";

    //Add authentification listener when activity starts
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //Remove authentification listener when activity starts
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        ViewPager vpPager = findViewById(R.id.vpPager);
        vpPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        vpPager.setCurrentItem(0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        initDrawerLayout();
        mAuth = FirebaseAuth.getInstance();
        initAuth();

        NavigationView mNavigationView =  findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawer_home:
                Toast.makeText(getApplicationContext(), "Home.", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.drawer_settings:
                Toast.makeText(getApplicationContext(), "Setting.", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initDrawerLayout(){
        final RealtimeLocation mLocationTracker = new RealtimeLocation(this);
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {}
                    @Override
                    public void onDrawerClosed(View drawerView) {}
                    @Override
                    public void onDrawerStateChanged(int newState) {}

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        final TextView user_textview = (TextView) drawerView.findViewById(R.id.user_name);
                        final TextView location_textview= (TextView) drawerView.findViewById(R.id.user_location);

                        // Respond when the drawer is opened
                        mLocationTracker.getLocation();
                        final double longitude = mLocationTracker.getLongitude();
                        final double latitude = mLocationTracker.getLatitude();

                        if (Config.username == null) {
                            user_textview.setText("@Anonymous");
                        } else {
                            user_textview.setText(Config.username);
                        }
                        location_textview.setText("Lat=" + new DecimalFormat(".##").format(latitude)
                                + " , Lon=" + new DecimalFormat(".##").format(longitude));
                    }
                }
        );
    }

    private void initAuth(){
        //Add listener to check sign in status
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Config.uid = user.getUid();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        //sign in anonymously
        mAuth.signInAnonymously().addOnCompleteListener(this,  new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException());
                }
            }
        });
    }

}

