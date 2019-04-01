package naihebaver.policeassistantapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import naihebaver.policeassistantapp.PulsingButton.PulsingButtonTextView;
import naihebaver.policeassistantapp.PulsingButton.PulsingButtonView;
import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

public class DangerActivity extends AppCompatActivity implements PulsingButtonView.OnPulseButtonClickListener {

    RelativeLayout mLayout1, mLayout2;
    SharedPreferencesHelper mSharedPrefsHelper;
    String mAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);
        setupToolbar("Потрібна допомога");
        mSharedPrefsHelper = new SharedPreferencesHelper(this);
        mLayout1 = findViewById(R.id.layout1);
        mLayout2 = findViewById(R.id.layout2);


        getAddress();






        if (mSharedPrefsHelper.getColleaguePhone() != null) {
            //setContentView(R.layout.activity_danger2);
            mLayout1.setVisibility(View.INVISIBLE);
            mLayout2.setVisibility(View.VISIBLE);
            //PulsingButtonView pulsingButtonView = findViewById(R.id.pulsing_button);
            PulsingButtonTextView pulsingButtonTextView = findViewById(R.id.pulsing_text);
            pulsingButtonTextView.setText("Потрібна допомога");
            pulsingButtonTextView.setTextSize(20);

            PulsingButtonView pulsingButtonView = findViewById(R.id.pulsing_button);
            pulsingButtonView.setOnPulseButtonClick(this);


            TextView changePhone = findViewById(R.id.changePhone);
            changePhone.setText(Html.fromHtml("<u>Змінити номер колеги</u>"));
            changePhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSharedPrefsHelper.setColleaguePhone(null);
                    mLayout1.setVisibility(View.INVISIBLE);
                    mLayout2.setVisibility(View.VISIBLE);
                    recreate();

                }
            });

        } else {
            mLayout1.setVisibility(View.VISIBLE);
            mLayout2.setVisibility(View.INVISIBLE);



            final EditText colleaguePhone = findViewById(R.id.colleaguePhone);
            Button btnConfirm = findViewById(R.id.confirmBtn);

            colleaguePhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    colleaguePhone.setText("+380");
                    colleaguePhone.setSelection(colleaguePhone.getText().length());
                }
            });

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String phone = colleaguePhone.getText().toString().trim();

                    if (phone.isEmpty()) {
                        UsefulMethods.showMessage("Будь ласка, введіть номер!", getApplicationContext());
                        return;
                    } else if (phone.length() != 13) {
                        UsefulMethods.showMessage("Будь ласка, введіть правильно номер!", getApplicationContext());
                        return;
                    } else {
                        mSharedPrefsHelper.setColleaguePhone(phone);
                        UsefulMethods.showMessage(phone, getApplicationContext());
                        mLayout1.setVisibility(View.INVISIBLE);
                        mLayout2.setVisibility(View.VISIBLE);
                        recreate();
                    }
                }
            });
        }

    }

    @SuppressLint("MissingPermission")
    private void getAddress() {
        LocationRequest request = new LocationRequest();
        request.setNumUpdates(1);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        client.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.ENGLISH);
                    List<Address> address;
                    try {
                        address = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (address.size() > 0) {

                            String city = address.get(0).getLocality();
                            //String state = address.get(0).getAdminArea();
                            String knownName = address.get(0).getFeatureName();
                            String street = address.get(0).getThoroughfare();

                            if(street != null) mAddress = mAddress + street + ", ";
                            if(knownName != null) mAddress = mAddress + knownName + ", ";
                            if(city != null) mAddress = mAddress + city;
                            //if (street != null && knownName != null && city != null) {
                            //    mAddress = street + ", " + knownName + ", " + city;
                                Log.i("Address = ", mAddress);
                            //}
                        }

                    } catch (Exception e) {}
                }
            }
        }, null);
    }


    @Override
    public void onPulseButtonClick() {
        final Dialog dialog = new Dialog(DangerActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_danger);

        TextView txtNo = dialog.findViewById(R.id.tv_no);
        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TextView txtYes = dialog.findViewById(R.id.tv_yes);
        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAddress != null) {
                    //String message = "Ваш колега в небезпеці і потребує Вашої допомоги!\n\nЙого останнє місцезнаходження: " + mAddress;
                    //Uri uri = Uri.parse("smsto:+380997274018");
                    //Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    //intent.putExtra("sms_body", message);
                    //startActivity(intent);
                    sendSms();
                    dialog.dismiss();
                }

            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void sendSms() {
        //Uri uri = Uri.parse("smsto:+380997274018");
        //Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        //intent.putExtra("sms_body", "Here goes your message...");
        //startActivity(intent);



        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            MyMessage();
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},0);
        }
    }

    private void MyMessage() {
        String phoneNumber = mSharedPrefsHelper.getColleaguePhone();
        //String message = "Ваш колега в небезпеці і потребує Вашої допомоги!\n\nЙого останнє місцезнаходження: " + mAddress;
        String message = "Vash kolega v nebezpetsi i potrebuyet Vashoi dopomogi!\n\nYogo ostanne mistseznakhodzhennya: " + mAddress;
        Log.i("MessageLength = ", String.valueOf(message.length()));
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            UsefulMethods.showMessage("Повідомлення успішно відправлено!", this);
        }catch (Exception e){
            UsefulMethods.showMessage("Повідомлення не вдалося відправити.", this);
            e.printStackTrace();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0)
        {
            if(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                MyMessage();
            }else{
                UsefulMethods.showMessage("You don't have required permission to send the sms", this);
            }
        }
    }

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
