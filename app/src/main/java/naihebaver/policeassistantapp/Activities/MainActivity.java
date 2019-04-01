package naihebaver.policeassistantapp.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import naihebaver.policeassistantapp.Helpers.InternetConnection;
import naihebaver.policeassistantapp.Models.User;
import naihebaver.policeassistantapp.Ocr.OcrCaptureActivity;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.PulsingButton.PulsingButtonView;
import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Services.BackgroundService;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,PulsingButtonView.OnPulseButtonClickListener {

    Intent mServiceIntent;
    BackgroundService mBackgroundService;
    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    SharedPreferencesHelper mSharedPrefsHelper;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mUsersRef = mDatabase.getReference("Users/");
    PulsingButtonView pulsingButtonView;
    TextView txtUserName, txtUserPhone;
    ImageView mUserPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition(0, 0);

        setContentView(R.layout.navigation_drawer);

        pulsingButtonView = findViewById(R.id.pulsing_button);
        pulsingButtonView.setOnPulseButtonClick(this);

        //drawer initialisation
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(drawerListener);

        navigationView = findViewById(R.id.navigation_view);

        setupToolbar("Помічник поліцейського");

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.user_name);
        txtUserPhone = headerView.findViewById(R.id.user_phone);
        mUserPhoto = headerView.findViewById(R.id.user_image);

        mSharedPrefsHelper = new SharedPreferencesHelper(this);

        if(mSharedPrefsHelper.getKey().equals("0")){
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }else {
            if(InternetConnection.checkConnection(this)) {
                addCurrentUserListener();

            }else UsefulMethods.showMessage(getString(R.string.msg_connection_problem), this);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 199);
        }

        mBackgroundService = new BackgroundService();
        mServiceIntent = new Intent(this, mBackgroundService.getClass());

    }

    private void getState() {
        LocationRequest request = new LocationRequest();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.ENGLISH);
                    List<Address> address;
                    try {

                        address = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (address.size() > 0) {
                            String st = address.get(0).getAdminArea();

                            if (st != null) {
                                //state = st;
                            }
                           // Log.i("State = ", state);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }, null);
    }

    private void addCurrentUserListener(){

        mUsersRef.child(mSharedPrefsHelper.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                if(user != null) {
                    //inSelfUser = user;
                    txtUserName.setText(user.getName());
                    txtUserPhone.setText(user.getPhone());
                    if(user.getUserPhoto() != null){
                        Glide.with(getApplicationContext()).load(user.getUserPhoto()).into(mUserPhoto);
                    }
                    mSharedPrefsHelper.setUserName(user.getName());
                    mSharedPrefsHelper.setUserPhone(user.getPhone());
                    mSharedPrefsHelper.setUserState(user.getState());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        mSharedPrefsHelper.setServiceWorking(1);
        if (!isMyServiceRunning(mBackgroundService.getClass())) {
            startService(mServiceIntent);
            mSharedPrefsHelper.setServiceWorking(1);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }
    @Override
    protected void onDestroy() {
        //stopService(mServiceIntent);
        super.onDestroy();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_news) {
            startActivity(new Intent(getApplicationContext(), NewsActivity.class));
            return true;
        }

        if (id == R.id.action_history) {
            startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        if(id == R.id.action_exit){
            //mSharedPrefsHelper.clear();
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finish();
            mSharedPrefsHelper.setKey("0");
            mSharedPrefsHelper.setServiceWorking(0);
            stopService(mServiceIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPulseButtonClick() {
        if(InternetConnection.checkConnection(this)) {
            Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
            startActivity(intent);
        }else UsefulMethods.showMessage(getString(R.string.msg_connection_problem), this);
        //overridePendingTransition(0, 0);
        //overridePendingTransition(R.anim.slidein, R.anim.slideout);
    }

    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        overridePendingTransition(0, 0);
        super.onRestart();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id){
            case R.id.home_id:
                break;
            case R.id.new_violatoin_id:
                startActivity(new Intent(MainActivity.this, OcrCaptureActivity.class));
                break;
            case R.id.history_id:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;
            case R.id.danger_id:
                startActivity(new Intent(MainActivity.this, DangerActivity.class));
                break;
            case R.id.news_id:
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                break;
            case R.id.settings_id:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.exit_id:
                //FirebaseAuth.getInstance().signOut();
                //sharedPreferencesHelper.clear();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
                mSharedPrefsHelper.setServiceWorking(0);
                stopService(mServiceIntent);
                break;

            case R.id.shemy_dtp:
                String url = "https://firebasestorage.googleapis.com/v0/b/policeassistantapp-a8819.appspot.com/o/useful_files%2Fshemy-dtp.pdf?alt=media&token=43d08ee5-77aa-4799-88b5-9b6d3f241033";
                Intent intentPdf = new Intent(Intent.ACTION_VIEW);
                intentPdf.setDataAndType(Uri.parse(url), "application/pdf");
                intentPdf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent newIntent = Intent.createChooser(intentPdf, "Open File");
                try {
                    startActivity(newIntent);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }

                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if(!InternetConnection.checkConnection(getApplicationContext())) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                UsefulMethods.showMessage(getString(R.string.msg_connection_problem), getApplicationContext());
            }
        }
    };

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

}
