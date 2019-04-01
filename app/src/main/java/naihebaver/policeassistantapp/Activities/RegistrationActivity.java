package naihebaver.policeassistantapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import naihebaver.policeassistantapp.Models.User;
import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

public class RegistrationActivity extends AppCompatActivity {

    ImageView userPhoto;
    private EditText userEmail, userPassword, userPassword2, userName, userPhone;
    private ProgressBar loadingProgress;
    private Button regContinueBtn;
    String mState;
    List<User> mUsersList = new ArrayList<>();
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mUsersRef = mDatabase.getReference("Users/");
    Uri pickedImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setupToolbar("Реєстрація");

        //ini views
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        userPhone = findViewById(R.id.regPhone);
        loadingProgress = findViewById(R.id.regProgressBar);
        regContinueBtn = findViewById(R.id.regContinueBtn);
        userPhoto = findViewById(R.id.regUserPhoto);
        loadingProgress.setVisibility(View.INVISIBLE);

        ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        userPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    userPhone.setText("+380");
                    userPhone.setSelection(userPhone.getText().length());

                }
            }
        });

        getUsersList();
        getUserState();


        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(RegistrationActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(RegistrationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        } else {
                            openGallery();
                        }
                    }
                }else
                {
                    openGallery();
                }
            }
        });




        regContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regContinueBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                 String email = userEmail.getText().toString();
                 String password = userPassword.getText().toString();
                 String password2 = userPassword2.getText().toString();
                 String name = userName.getText().toString();
                 String phone = userPhone.getText().toString();


                    if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || !password.equals(password2) || mState.isEmpty()) {

                        UsefulMethods.showMessage("Будь ласка, введіть всі дані!", getApplicationContext());
                        regContinueBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                        return;

                    }else if(!isPasswordValid(password)) {
                        UsefulMethods.showMessage("Пароль занадто короткий!", getApplicationContext());
                        regContinueBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                        return;
                    }else if(!email.isEmpty() && !isEmailValid(email)){
                        UsefulMethods.showMessage("Введіть правильно E-mail!", getApplicationContext());
                        regContinueBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                        return;
                    }


                    if(mUsersList != null ){
                        for (User us:mUsersList){
                            if(us.getPhone() != null){
                                if(us.getPhone().equals(phone)) {
                                    UsefulMethods.showMessage("Користувач з таким номером телефону уже існує...", getApplicationContext());
                                    regContinueBtn.setVisibility(View.VISIBLE);
                                    loadingProgress.setVisibility(View.INVISIBLE);
                                    return;
                                }
                            }
                        }
                    }

                Intent intent = new Intent(RegistrationActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("phone", phone);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                if(pickedImgUri != null){ intent.putExtra("user_photo", pickedImgUri.toString());}
                intent.putExtra("state", mState);
                startActivity(intent);
                finish();

                }
        });
    }

    private void getUsersList(){
        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsersList.clear();
                for (DataSnapshot dt : dataSnapshot.getChildren()) {

                    User user = dt.getValue(User.class);
                    if (user != null) mUsersList.add(user);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserState(){
        LocationRequest request = new LocationRequest();
        request.setNumUpdates(1);
        //request.setInterval(1000);
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
                                mState = st;
                            }
                        }

                    } catch (Exception e) {

                    }
                }
            }
        }, null);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1 && data != null ) {
            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData() ;
            userPhoto.setImageURI(pickedImgUri);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
