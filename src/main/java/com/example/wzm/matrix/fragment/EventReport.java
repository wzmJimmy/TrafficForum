package com.example.wzm.matrix.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.wzm.matrix.R;
import com.example.wzm.matrix.adapter.ReportRecyclerViewAdapter;
import com.example.wzm.matrix.config.Config;
import com.example.wzm.matrix.model.LocationTracker;
import com.example.wzm.matrix.model.TrafficEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class EventReport {

    private Fragment fragment;
    private Activity activity;
    private Context context;
    private Dialog dialog;
    private ViewSwitcher mViewSwitcher;
    private FloatingActionButton fab_report;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    //Event specs
    private ImageView mImageCamera;
    private EditText mCommentEditText;
    private ImageView mEventTypeImg;
    private TextView mTypeTextView;
    private String event_type = null;
    private static final int REQUEST_CAPTURE_IMAGE = 100;

    //Variables ready for uploading images
    private StorageReference storageRef;
    private final String path = Environment.getExternalStorageDirectory() + "/temp.png";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private LocationTracker locationTracker;
    private boolean reporting = false;
    private MapMarkerRefresher refresher;


    interface MapMarkerRefresher{
        void refreshMarkers();
    }

    public EventReport(Fragment fragment){
        this.fragment=fragment;
        this.activity = fragment.getActivity();
        this.context = fragment.getContext();

        verifyStoragePermissions(activity);
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void setFab_report(FloatingActionButton fab_report) {
        this.fab_report = fab_report;
    }
    public void setLocationTracker(LocationTracker locationTracker){
        this.locationTracker=locationTracker;
    }
    public void setRefresher(MapMarkerRefresher markerRefresher){
        refresher = markerRefresher;
    }

    //Animation show dialog
    protected void showDiag() {
        final View dialogView = View.inflate(activity,R.layout.dialog,null);
        mViewSwitcher = dialogView.findViewById(R.id.viewSwitcher);
        dialog = new Dialog(activity,R.style.MyAlertDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                animateDialog(dialogView, true, null);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction()!=KeyEvent.ACTION_DOWN){
                    if(reporting) {
                        mViewSwitcher.showPrevious();
                        reporting = false;
                    }else{
                        animateDialog(dialogView, false, dialog);
                    }
                    return true;
                }
                return false;
            }
        });

        Animation slide_in_left = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(activity, android.R.anim.slide_out_right);
        mViewSwitcher.setInAnimation(slide_in_left);
        mViewSwitcher.setOutAnimation(slide_out_right);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setupRecyclerView(dialogView);
        setUpEventSpecs(dialogView);
        dialog.show();
    }


    //Add animation to Floating Action Button
    private void animateDialog(View dialogView, boolean open, final Dialog dialog) {
        final View view = dialogView.findViewById(R.id.dialog);
        int w = view.getWidth();
        int h = view.getHeight();
        int endRadius = (int) Math.hypot(w, h);
        int cx = (int) (fab_report.getX() + (fab_report.getWidth()/2));
        int cy = (int) (fab_report.getY())+ fab_report.getHeight() + 20;

        if(open){
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx,cy, 0, endRadius);
            revealAnimator.setDuration(500);
            revealAnimator.start();

        } else {
            Animator closeAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0);
            closeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.dismiss();
                }
            });
            closeAnimator.setDuration(500);
            closeAnimator.start();
        }

    }

    //Set up type items
    private void setupRecyclerView(View dialogView) {
        RecyclerView mRecyclerView = dialogView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        ReportRecyclerViewAdapter mRecyclerViewAdapter = new ReportRecyclerViewAdapter(activity, Config.listItems);
        mRecyclerViewAdapter.setClickListener(new ReportRecyclerViewAdapter.OnClickListener() {
            @Override
            public void setItem(String item) {
                event_type = item;
                if(mViewSwitcher != null) {
                    reporting = true;
                    mViewSwitcher.showNext();
                    mTypeTextView.setText(event_type);
                    mEventTypeImg.setImageBitmap(BitmapFactory.decodeResource(
                            context.getResources() , Config.trafficMap.get(event_type)));
                }
            }
        });

        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private void setUpEventSpecs(final View dialogView) {
        mImageCamera =  dialogView.findViewById(R.id.event_camera_img);
        mCommentEditText =  dialogView.findViewById(R.id.event_comment);
        mEventTypeImg = dialogView.findViewById(R.id.event_type_img);
        mTypeTextView = dialogView.findViewById(R.id.event_type);
        Button mBackButton =  dialogView.findViewById(R.id.event_back_button);
        Button mSendButton =  dialogView.findViewById(R.id.event_send_button);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = uploadEvent(Config.username);
                //upload image and link the image to the corresponding key
                uploadImage(key);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewSwitcher != null) {
                    mViewSwitcher.showPrevious();
                }
            }
        });
        mImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fragment.startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        });
    }

    // upload feature //
    //Upload event
    private String uploadEvent(String user_id) {
        TrafficEvent event = new TrafficEvent();
        event.setEvent_type(event_type);
        event.setEvent_description(mCommentEditText.getText().toString());
        event.setEvent_reporter_id(user_id);
        event.setEvent_timestamp(System.currentTimeMillis());
        event.setEvent_latitude(locationTracker.getLatitude());
        event.setEvent_longitude(locationTracker.getLongitude());
        event.setEvent_like_number(0);
        event.setEvent_comment_number(0);

        String key = database.child("events").push().getKey();
        String key2 = database.child("events_likes").push().getKey();
        event.setId(key);
        event.setId_likelist(key2);

        database.child("events_likes").child(key2).child("id").setValue(key2);
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(context, "The event is failed, please check your network status.",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Log.d("uploadEvent#", "onComplete: The event is reported");
                    Toast.makeText(context, "The event is reported", Toast.LENGTH_SHORT).show();
                    //TODO: update map fragment
                }
            }
        });
        return key;
    }

    // upload feature //
    //Upload image to cloud storage
    private void uploadImage(final String key) {
        File file = new File(path);
        if (!file.exists()) {
            dialog.dismiss();
            refresher.refreshMarkers();
            return;
        }


        Uri uri = Uri.fromFile(file);
        StorageReference imgRef = storageRef.child("images/" + uri.getLastPathSegment() + "_" + System.currentTimeMillis());

        UploadTask uploadTask = imgRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                database.child("events").child(key).child("imgUri").setValue(downloadUrl.toString());
                new File(path).delete();
                dialog.dismiss();
                refresher.refreshMarkers();
            }
        });
    }

    public void inner_onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                mImageCamera.setImageBitmap(imageBitmap);

                //Compress the image, this is optional
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);


                File destination = new File(Environment.getExternalStorageDirectory(),"temp.png");
                if(!destination.exists()) {
                    try {
                        destination.createNewFile();
                    }catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }

                try(FileOutputStream fo = new FileOutputStream(destination)) {
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // upload feature //
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        if(ActivityCompat.checkSelfPermission(activity, PERMISSIONS_STORAGE[1])
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }
}
