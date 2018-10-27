package com.example.wzm.matrix.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wzm.matrix.model.User;
import com.example.wzm.matrix.config.Utils;
import com.example.wzm.matrix.config.Config;
import com.example.wzm.matrix.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private View loginLayout;
    private View logoutLayout;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mSubmitButton;
    private Button mRegisterButton;
    private Button mLogoutButton;

    private DatabaseReference mDatabase;


    /**
     * Static function, create loginFragment instance
     * @return new instance of login fragment
     */
    public static LoginFragment newInstance() {
        LoginFragment loginFragment = new LoginFragment();
        return loginFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginLayout = view.findViewById(R.id.loginLayout);
        logoutLayout = view.findViewById(R.id.logoutLayout);
        showLayout();

        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsernameEditText = (EditText) view.findViewById(R.id.editTextLogin);
        mPasswordEditText = (EditText) view.findViewById(R.id.editTextPassword);
        mSubmitButton = (Button) view.findViewById(R.id.submit);
        mRegisterButton = (Button) view.findViewById(R.id.register);
        mLogoutButton = (Button) view.findViewById(R.id.logout);
        init_Login_Logout();
        initRegister();
        return view;
    }

    private void init_Login_Logout(){
//        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//        myRef.setValue("Hello, World!");

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsernameEditText.getText().toString();
                final String password = Utils.md5Encryption(mPasswordEditText.getText().toString());

                mDatabase.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(username) &&
                                (password.equals(dataSnapshot.child(username).child("user_password").getValue()))) {
                            mDatabase.child("user").child(username).child("user_timestamp").setValue(System.currentTimeMillis());
                            Config.username = username;
                            Config.uid = username;
                            showLayout();
                        } else {
                            Toast.makeText(getActivity(),"Invalid username or password. Please login again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }

                });
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.username = null;
                showLayout();
            }
        });
    }
    private void initRegister(){
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsernameEditText.getText().toString();
                final String password = mPasswordEditText.getText().toString();

                mDatabase.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(username)) {
                            Toast.makeText(getActivity(), "Username is already registered, please change one", Toast.LENGTH_SHORT).show();
                        } else if (!username.equals("") && !password.equals("")) {
                            // put username as key to set value
                            final User user = new User();
                            user.setUser_account(username);
                            user.setUser_password(Utils.md5Encryption(password));
                            user.setUser_timestamp(System.currentTimeMillis());

                            mDatabase.child("user").child(user.getUser_account()).setValue(user);
                            Toast.makeText(getActivity(), "Successfully registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Username and password cannot be null", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        });
    }

    private void showLayout(){
        if (Config.username == null) {
            logoutLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        } else {
            logoutLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        }
    }

}
