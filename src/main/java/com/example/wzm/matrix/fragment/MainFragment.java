package com.example.wzm.matrix.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wzm.matrix.model.LocationTracker;
import com.example.wzm.matrix.R;
import com.example.wzm.matrix.config.Config;
import com.example.wzm.matrix.config.Utils;
import com.example.wzm.matrix.model.TrafficEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class MainFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener{
    private final MainFragment fragment = this;
    private MapView mMapView;
    private View mView;
    private GoogleMap mMap;

    private LocationTracker locationTracker;
    private EventReport eventReport;
    private DatabaseReference database;

    //Marker event info
    private TrafficEvent mEvent;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView mEventImageLike;
    private ImageView mEventImageComment;
    private ImageView mEventImageType;
    private TextView mEventTextLike;
    private TextView mEventTextType;
    private TextView mEventTextLocation;
    private TextView mEventTextTime;

    private String currentEventId = null;

    // newInstance constructor for creating fragment with arguments
    public static MainFragment newInstance() {
        MainFragment mainFragment = new MainFragment();
        return mainFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_main, container, false);
        database = FirebaseDatabase.getInstance().getReference();

        locationTracker = new LocationTracker(getActivity());
        locationTracker.setOnLocationChangedListener(new LocationTracker.OnLocationChangedListener() {
            @Override
            public void OnLocationChanged() {
                    mMapView.getMapAsync(fragment);
            }
        });

        eventReport = new EventReport(this);
        eventReport.setLocationTracker(locationTracker);
        eventReport.setRefresher(new EventReport.MapMarkerRefresher() {
            @Override
            public void refreshMarkers() {
                loadEventInVisibleMap();
            }
        });

        setupBottomBehavior();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.event_map_view);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();// needed to get the map to display immediately
            mMapView.getMapAsync(fragment);
        }
        FloatingActionButton fab_focus = mView.findViewById(R.id.fab_foucs);
        fab_focus.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {mMapView.getMapAsync(fragment);
                                         }
                                     });

        FloatingActionButton fab_report = mView.findViewById(R.id.fab);
        fab_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { eventReport.showDiag(); }
        });
        eventReport.setFab_report(fab_report);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    currentEventId = null;
                }
            }
        });
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                loadEventInVisibleMap();
            }
        });

        if(checkDaytime()){ mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_day)); }
        else{ mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json)); }

        LatLng latLng = new LatLng(locationTracker.getLatitude(), locationTracker.getLongitude());
        Log.d("Location#", "onMapReady: "+latLng );

        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16)
                .bearing(0)           // Sets the orientation of the camera to north
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        loadEventInVisibleMap();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        eventReport.inner_onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mEvent = (TrafficEvent)marker.getTag();
        if (mEvent == null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            return true;
        }

        String user = mEvent.getEvent_reporter_id();
        String type = mEvent.getEvent_type();
        long time = mEvent.getEvent_timestamp();
        double latitude = mEvent.getEvent_latitude();
        double longitude = mEvent.getEvent_longitude();
        int likeNumber = mEvent.getEvent_like_number();


        // init marker title
        String description = mEvent.getEvent_description();
        String title = description.length()>10?description.substring(0,9)+"...":description;
        marker.setTitle(title);

        mEventTextLike.setText(String.valueOf(likeNumber));
        mEventTextType.setText(type);

        // fetch img from uri with AsyncTask.
        final String url = mEvent.getImgUri();
        if (url == null) {
            mEventImageType.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(),
                    Config.trafficMap.get(type)));
        } else {
            new  AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    Bitmap bitmap = Utils.getBitmapFromURL(url);
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    mEventImageType.setImageBitmap(bitmap);
                }
            }.execute();
        }

        // init username to show
        if (user == null) { user = "@Anonymous"; }
        String info = "Reported by " + user + " " + Utils.timeTransformer(time);
        mEventTextTime.setText(info);

        // init distance to show.
        double distance = 0;
        if (locationTracker != null) {
            distance = Utils.distanceBetweenTwoLocations(latitude, longitude, locationTracker.getLocation());
        }
        mEventTextLocation.setText( String.format( "%.2f miles away", distance ));

        // set the state of comment view.
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            currentEventId = mEvent.getId();
        } else if(currentEventId.equals(mEvent.getId())){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            currentEventId = null;
        } else {
            currentEventId =  mEvent.getId();
        }

        // init the like img.
        database.child("events_likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(mEvent.getId_likelist()).hasChild(Config.uid)){
                    mEventImageLike.setImageDrawable(getContext().getDrawable(R.drawable.liked));
                }else{
                    mEventImageLike.setImageDrawable(getContext().getDrawable(R.drawable.like));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        return false;
    }

    //get center coordinate
    private void loadEventInVisibleMap() {
        mMap.clear();

        LatLng latLng = new LatLng(locationTracker.getLatitude(), locationTracker.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(latLng).title("You");
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.boy));
        mMap.addMarker(marker);

        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng center = mMap.getCameraPosition().target;
                double centerLatitude = center.latitude;
                double centerLongitude = center.longitude;

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    TrafficEvent event = noteDataSnapshot.getValue(TrafficEvent.class);
                    double eventLatitude = event.getEvent_latitude();
                    double eventLongitude = event.getEvent_longitude();
                    double distance = Utils.distanceBetweenTwoLocations(centerLatitude, centerLongitude,
                            eventLatitude, eventLongitude);

                    if (distance < 20) {
                        LatLng latLng = new LatLng(eventLatitude, eventLongitude);
                        MarkerOptions marker = new MarkerOptions().position(latLng);

                        // Changing marker icon
                        String type = event.getEvent_type();
                        Bitmap icon = BitmapFactory.decodeResource(getContext().getResources(),
                                Config.trafficMap.get(type));
                        Bitmap resizeBitmap = Utils.getResizedBitmap(icon, 130, 130);
                        marker.icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap));

                        // adding marker
                        Marker mker = mMap.addMarker(marker);
                        mker.setTag(event);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setupBottomBehavior() {
        //set up bottom up slide
        final View nestedScrollView =  mView.findViewById(R.id.nestedScrollView);
        bottomSheetBehavior = BottomSheetBehavior.from(nestedScrollView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //set expansion speed
        bottomSheetBehavior.setPeekHeight(1000);

        mEventImageLike = mView.findViewById(R.id.event_info_like_img);
        mEventImageComment = mView.findViewById(R.id.event_info_comment_img);
        mEventImageType = mView.findViewById(R.id.event_info_type_img);

        mEventTextLike = mView.findViewById(R.id.event_info_like_text);
        mEventTextType = mView.findViewById(R.id.event_info_type_text);
        mEventTextLocation = mView.findViewById(R.id.event_info_location_text);
        mEventTextTime = mView.findViewById(R.id.event_info_time_text);

        initMEventImageLike();

        mEventImageComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Comment function TBD.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initMEventImageLike(){
        mEventImageLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("events_likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    private DatabaseReference uesrlike = database.child("events_likes").child(mEvent.getId_likelist()).child(Config.uid);
                    private DatabaseReference likenum = database.child("events").child(mEvent.getId()).child("event_like_number");
                    private int change_num ;
                    private Transaction.Handler transaction = new Transaction.Handler() {
                        private int current_num;
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if(currentValue == null){
                                mutableData.setValue(-1);
                            }else {
                                current_num = currentValue + change_num;
                                mutableData.setValue(current_num);
                                Log.d("doTransaction#", "notnull: " + current_num);
                            }
                            return Transaction.success(mutableData);
                        }
                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            Log.d("doTransaction#", "finished-changed: "+ dataSnapshot);
                            mEventTextLike.setText(String.valueOf(current_num));
                            if(change_num<0){
                                mEventImageLike.setImageDrawable(getContext().getDrawable(R.drawable.like));
                                Toast.makeText(getContext(), "Like cancelled.", Toast.LENGTH_SHORT).show();
                            }else{
                                mEventImageLike.setImageDrawable(getContext().getDrawable(R.drawable.liked));
                                Toast.makeText(getContext(), "Like successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(mEvent.getId_likelist()).hasChild(Config.uid)){
                            uesrlike.removeValue();
                            change_num = -1;
                            likenum.runTransaction(transaction);
                        }else{
                            uesrlike.setValue(System.currentTimeMillis());
                            change_num = 1;
                            likenum.runTransaction(transaction);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

    private boolean checkDaytime(){
        final String morning = "06:00:00";
        final String evening = "18:00:00";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
        String string = mdformat.format(calendar.getTime());
        return string.compareTo(morning)>=0 && string.compareTo(evening)<0;
    }
}
