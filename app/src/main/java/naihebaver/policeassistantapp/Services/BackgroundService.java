package naihebaver.policeassistantapp.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import naihebaver.policeassistantapp.Activities.SendActivity;
import naihebaver.policeassistantapp.Models.Violation;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;

public class BackgroundService extends Service {

    RemoteViews mCollapsedView;
    private static final String TAG = "BackgroundService";

    SharedPreferencesHelper mSharedPrefsHelper;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mViolationRef;
    //private int requestInterval;
    double lat, lng;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        mSharedPrefsHelper = new SharedPreferencesHelper(getApplicationContext());
        mViolationRef = mDatabase.getReference("Violation/"+mSharedPrefsHelper.getUserState()+"/");

//        switch (sharedPreferencesHelper.getWorkingMode()) {
//            case 1:
//                requestInterval = 300000; //5m
//                break;
//            case 2:
//                requestInterval = 60000; //1m
//                break;
//            case 3:
//                requestInterval = 5000; //5s
//                break;
//        }
//        Log.e("requestInterval", "requestInterval = " + requestInterval);
        addChangeListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    private void getLocation() {

        LocationRequest request = new LocationRequest();
        //request.setInterval(requestInterval);
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        client.requestLocationUpdates(request, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if(location != null) {
                    try{
                        lng = location.getLongitude();
                        lat = location.getLatitude();

                    }catch (Exception e){ }
                }
            }
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        }, null);
    }

    private void addChangeListener() {

        mViolationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Violation violation = dataSnapshot.getValue(Violation.class);

                if(violation != null){

                    getLocation();

                    if(!mSharedPrefsHelper.getDate().equals("0")){
                        if(mSharedPrefsHelper.getTime().equals(violation.getTime())) return; else ShowNotification(violation);
                    }else ShowNotification(violation);


                    mSharedPrefsHelper.setDate(violation.getDate());
                    mSharedPrefsHelper.setTime(violation.getTime());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void ShowNotification(Violation violation) {

        if(violation.getFrom_user().equals(mSharedPrefsHelper.getKey())) return;

        try{

            Location loc1 = new Location("");
            loc1.setLatitude(lat);
            loc1.setLongitude(lng);

            Location loc2 = new Location("");
            loc2.setLatitude(violation.getLt());
            loc2.setLongitude(violation.getLg());

            float distanceInMeters = loc1.distanceTo(loc2);
            Log.e("services", "distance = " + distanceInMeters);

            if(distanceInMeters > mSharedPrefsHelper.getNotifyRadius()*1000) return;

        }catch (Exception e){
            return;
        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        mCollapsedView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        mCollapsedView.setTextViewText(R.id.n_number, violation.getReg_number());
        mCollapsedView.setTextViewText(R.id.n_type, violation.getText_type());
        mCollapsedView.setTextViewText(R.id.n_place, violation.getAddress());


        Log.e(TAG, "create notification");
        String number = violation.getReg_number();
        Log.e("services", number);

        Intent intentNotification = new Intent(this, SendActivity.class);
        intentNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentNotification.putExtra("date", violation.getDate());
        PendingIntent rightIntent = PendingIntent.getActivity(this, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "Police Assistant";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "PoliceAssistantChannel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.small_notify_icon2)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Нове порушення")
                .setContentIntent(rightIntent)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setVibrate(new long[]{0,1000,500,1000})
                .setCustomBigContentView(mCollapsedView);

        notificationManager.notify(new Random().nextInt(),notificationBuilder.build());

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "police.assistant";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        if(mSharedPrefsHelper.getServiceWorking() == 1) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
        }
    }

}