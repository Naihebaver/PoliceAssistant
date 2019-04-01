package naihebaver.policeassistantapp.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import naihebaver.policeassistantapp.Models.Violation;
import naihebaver.policeassistantapp.Models.User;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Interfaces.SendInterface;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

public class SendActivity extends AppCompatActivity implements SendInterface.View {

    ImageView mOffenderPhoto;
    Spinner mSpinner;
    TextView txtRegN, txtRegC, txtDate, txtTime, txtPlace, txtShowMap, txtViolationName, txtFixedViolation;
    CardView imgCardView;
    Button mConfirmBtn, mImgAddBtn;
    LinearLayout mVar1, mVar2, mLayoutFixed;

    Calendar dateAndTime;
    SimpleDateFormat df;

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mViolationRef;
    DatabaseReference mUserRef;
    DatabaseReference mUserHistoryRef;
    DatabaseReference mUserHistoryItemRef;
    String mOffenderPhotoUrl;
    Bitmap mBitmap;

    private static  final int CAMERA_REQUEST = 123;

    double lat;
    double lng;

    private String mImgFilePath = "";

    SharedPreferencesHelper mSharedPrefsHelper;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        setupToolbar("Фіксація порушення");

        mSharedPrefsHelper = new SharedPreferencesHelper(this);
        mUserRef = mDatabase.getReference("Users/");
        mViolationRef = mDatabase.getReference("Violation/"+mSharedPrefsHelper.getUserState()+"/");


        Intent intent = getIntent();
        String n = intent.getStringExtra("date");

        mUserHistoryRef = mDatabase.getReference("History/");
        initUI();
        checkPermission();


        if (n == null) generateData(); //new violation
        else {



            String from_history = intent.getStringExtra("from_history");
            if(from_history == null) getData();//from push notify
            else { //from history

                String key_h = intent.getStringExtra("key_h");
                //String from_h = intent.getStringExtra("from_h");
                mUserHistoryItemRef = mDatabase.getReference("History/").child(key_h);
                mConfirmBtn.setVisibility(View.GONE);
                mUserHistoryItemRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final Violation violation = dataSnapshot.getValue(Violation.class);

                        if(violation != null){

                            if(violation.getDate() != null) txtDate.setText(violation.getDate());
                            if(violation.getTime() != null) txtTime.setText(violation.getTime());
                            if(violation.getReg_number() != null) txtRegN.setText(violation.getReg_number());
                            if (violation.getCountryCar() != null) txtRegC.setText(violation.getCountryCar());
                            if(violation.getAddress() != null) txtPlace.setText(violation.getAddress());
                            if(violation.getFrom_user() != null) {
                                mUserRef.orderByChild("key").equalTo(violation.getFrom_user()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                User user = snapshot.getValue(User.class);
                                                txtFixedViolation.setText(user != null ? user.getName() : null);
                                                //Log.i("Name = ", user.getName());
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                            //mSpinner.setSelection(violation.getType());
                            //mSpinner.setEnabled(false);
                            mVar1.setVisibility(View.GONE);
                            mVar2.setVisibility(View.VISIBLE);
                            txtViolationName.setText(violation.getText_type());
                            if(violation.getOffender_photo() != null){
                                mOffenderPhoto.setVisibility(View.VISIBLE);
                                Glide.with(getApplicationContext()).load(violation.getOffender_photo()).into(mOffenderPhoto);
                            }else {
                                mOffenderPhoto.setVisibility(View.GONE);
                                mImgAddBtn.setVisibility(View.VISIBLE);
                                mImgAddBtn.setText("Фото відсутнє"); //From History
                            }
                            txtShowMap.setVisibility(View.VISIBLE);
                            txtShowMap.setText(Html.fromHtml("<u>Показати на карті і прокласти маршрут</u>"));
                            txtShowMap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(violation.getLt()) + "," + String.valueOf(violation.getLg()));
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    startActivity(mapIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void addUserListener(){

        //mUserRef = mDatabase.getReference("Users/");
        mUserRef.child(mSharedPrefsHelper.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null){
                    if(user.getOffender_photo() != null){
                        mOffenderPhotoUrl = user.getOffender_photo();
                        mOffenderPhoto.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(user.getOffender_photo()).into(mOffenderPhoto);

                    }else {
                        mOffenderPhoto.setVisibility(View.GONE);
                        mImgAddBtn.setVisibility(View.VISIBLE);
                        mImgAddBtn.setText("Прикріпити фото"); //From Ocr
                        mImgAddBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
                                openCameraIntent();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile;
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if(photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(pictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){

            mOffenderPhoto.setImageURI(Uri.parse(mImgFilePath));
            mImgAddBtn.setVisibility(View.GONE);
            mOffenderPhoto.setVisibility(View.VISIBLE);

            uploadPhotoToStorage(mImgFilePath);

        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mImgFilePath = image.getAbsolutePath();

        return image;
    }


    private void uploadPhotoToStorage(final String photoPath) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!photoPath.equals("")) {

                    final ProgressDialog progressDialog = new ProgressDialog(SendActivity.this);
                    //progressDialog.setTitle("Будь ласка, зачекайте");
                    //progressDialog.setMessage("Йде обробка зображення...");
                    progressDialog.setTitle("Завантаження...");
                    progressDialog.show();

                    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("offender_photos").child(UUID.randomUUID().toString());
                    storageReference.putFile(Uri.fromFile(new File(mImgFilePath)))

                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mOffenderPhotoUrl = uri.toString();
                                    Log.e("offenderPhotoUrl ", mOffenderPhotoUrl);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Завантажено "+(int)progress+"%");
                        }
                    });
                }
            }
        });
    }



    @SuppressLint("WrongConstant")
    @Override
    public void initUI() {

        mOffenderPhoto = findViewById(R.id.img_photo);
        mSpinner = findViewById(R.id.spin);
        mVar1 = findViewById(R.id.variant1);
        mVar2 = findViewById(R.id.variant2);
        mLayoutFixed = findViewById(R.id.layout_fixed);
        txtViolationName = findViewById(R.id.tv_violation_name);
        txtFixedViolation = findViewById(R.id.tv_fixed);
        txtShowMap = findViewById(R.id.showMap);

        String[] array = getResources().getStringArray(R.array.arr);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, array);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(0);

        txtDate = findViewById(R.id.tv_date);
        txtTime = findViewById(R.id.tv_time);
        txtRegN = findViewById(R.id.tv_reg_number);
        txtRegC = findViewById(R.id.tv_country_car);
        txtPlace = findViewById(R.id.tv_place);
        mConfirmBtn = findViewById(R.id.sendBtn);
        mImgAddBtn = findViewById(R.id.btn_add_img);
        //btnImgAdd.setVisibility(View.GONE);

        imgCardView = findViewById(R.id.img_card_view);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        if(mBitmap != null) {
            mOffenderPhoto.setImageBitmap(mBitmap);
        }
        else {
            mOffenderPhoto.setVisibility(View.GONE);
            //btnImgAdd.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void sendData() {
        mConfirmBtn.setEnabled(false);
        Violation violation = new Violation();
        violation.setAddress(txtPlace.getText().toString());
        violation.setDate(txtDate.getText().toString());
        violation.setTime(txtTime.getText().toString());
        violation.setType(mSpinner.getSelectedItemPosition());
        violation.setText_type(mSpinner.getSelectedItem().toString());
        violation.setLg(lng);
        violation.setLt(lat);
        violation.setReg_number(txtRegN.getText().toString());
        violation.setCountryCar(txtRegC.getText().toString());
        violation.setFrom_user(mSharedPrefsHelper.getKey());
        if(mOffenderPhotoUrl != null) violation.setOffender_photo(mOffenderPhotoUrl);

        mUserHistoryRef = mDatabase.getReference("History/");
        String key = mUserHistoryRef.push().getKey();
        violation.setKey(key);

        // for history
        mUserHistoryRef.child(key).setValue(violation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) ;
            }
        });

        // for notify
        mViolationRef.setValue(violation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mConfirmBtn.setEnabled(true);
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Інформація про порушення відправлена Вашим колегам!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SendActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }




    @Override
    public void getData() {

        mConfirmBtn.setVisibility(View.GONE);
        //Log.i("Push", "=========  Push");

        mViolationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Violation violation = dataSnapshot.getValue(Violation.class);

                if(violation != null){

                    if(violation.getDate() != null) txtDate.setText(violation.getDate());
                    if(violation.getTime() != null) txtTime.setText(violation.getTime());
                    if(violation.getReg_number() != null) txtRegN.setText(violation.getReg_number());
                    if(violation.getCountryCar() != null) txtRegC.setText(violation.getCountryCar());
                    if(violation.getAddress() != null) txtPlace.setText(violation.getAddress());
                    if(violation.getFrom_user() != null) {
                        mLayoutFixed.setVisibility(View.VISIBLE);
                        mUserRef.orderByChild("key").equalTo(violation.getFrom_user()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User user = snapshot.getValue(User.class);
                                        txtFixedViolation.setText(user != null ? user.getName() : null);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                    //mSpinner.setSelection(violation.getType());
                    //mSpinner.setEnabled(false);
                    mVar1.setVisibility(View.GONE);
                    mVar2.setVisibility(View.VISIBLE);
                    txtViolationName.setText(violation.getText_type());
                    if(violation.getOffender_photo() != null){
                        mOffenderPhoto.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(violation.getOffender_photo()).into(mOffenderPhoto);
                    } else {
                        mOffenderPhoto.setVisibility(View.GONE);
                        mImgAddBtn.setVisibility(View.VISIBLE);
                        mImgAddBtn.setText("Фото відсутнє"); //From Push
                    }
                    txtShowMap.setVisibility(View.VISIBLE);
                    txtShowMap.setText(Html.fromHtml("<u>Показати на карті і прокласти маршрут</u>"));
                    txtShowMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(violation.getLt()) + "," + String.valueOf(violation.getLg()));
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void generateData() {

        Intent intent = getIntent();
        final String regNumber = intent.getStringExtra("reg_n");

        addUserListener();
        setPlaceRegistration(regNumber);

        dateAndTime = Calendar.getInstance();
        long t = dateAndTime.getTimeInMillis();
        df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.getDefault());
        txtDate.setText(df.format(t).split(" ")[0]);
        txtTime.setText(df.format(t).split(" ")[1]);

        if (regNumber != null) txtRegN.setText(regNumber);
        txtFixedViolation.setText(mSharedPrefsHelper.getUserName());

        LocationRequest request = new LocationRequest();
        //request.setInterval(1000);
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
                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> address;

                    try{

                        lng = location.getLongitude();
                        lat = location.getLatitude();

                        address = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if(address.size() > 0){

                            String city = address.get(0).getLocality();
                            String state = address.get(0).getAdminArea();
                            String knownName = address.get(0).getFeatureName();
                            String street = address.get(0).getThoroughfare();

                            String result = "";

                            if(street != null) result = result + street + ", ";
                            if(knownName != null) result = result + knownName + ", ";
                            if(city != null) result = result + city + ", ";
                            if(state != null) result = result + state;

                            //if(street != null && knownName != null && city != null && state != null){
                            //    result = street + ", " + knownName + ", "+ city + ", "+ state;
                            //}

                            txtPlace.setText(result);
                        }
                    }catch (Exception e){

                    }
                }


            }
        }, null);

    }

    private void setPlaceRegistration(String regNumber){
        String regCountry = "";
        if(UsefulMethods.isUA(regNumber)){
            regCountry = "Україна";
        }else if(UsefulMethods.isPL(regNumber)){
            regCountry = "Польща";
        }else if(UsefulMethods.isSK(regNumber)){
            regCountry = "Словакія";
        }else if(UsefulMethods.isHU(regNumber)){
            regCountry = "Угорщина";
        }else {
            regCountry = "Не вдалося визначити";
        }

        if(regCountry.equals("Україна")){
            String codeState = regNumber.substring(0,2);
            String stateName;
            HashMap<String,String> codeStateTable = new HashMap<String,String>() {{
                put("AK", "АР Крим");
                put("AA", "місто Київ");
                put("AB", "Вінницька область");
                put("AC", "Волинська область");
                put("AE", "Дніпропетровська область");
                put("AH", "Донецька область");
                put("AI", "Київська область");
                put("AM", "Житомирська область");
                put("AO", "Закарпатська область");
                put("AP", "Запорізька область");
                put("AT", "Івано-Франківська область");
                put("AX", "Харківська область");
                put("BA", "Кіровоградська область");
                put("BB", "Луганська область");
                put("BC", "Львівська область");
                put("BE", "Миколаївська область");
                put("BH", "Одеська область");
                put("BI", "Полтавська область");
                put("BK", "Рівненська область");
                put("BM", "Сумська область");
                put("BO", "Тернопільська область");
                put("BT", "Херсонська область");
                put("BX", "Хмельницька область");
                put("CA", "Черкаська область");
                put("CB", "Чернігівська область");
                put("CE", "Чернівецька область");
                put("CH", "місто Севастополь");
            }};

            if(codeStateTable.containsKey(codeState)){
                 stateName = codeStateTable.get(codeState);
            }else{
                String firstLetter = regNumber.substring(0,1);
                String newFirstLetter = "";
                if(firstLetter.equals("K")) { newFirstLetter = "A"; }
                else if(firstLetter.equals("H")) { newFirstLetter = "B"; }
                else if(firstLetter.equals("I")) { newFirstLetter = "C"; }

                codeState = codeState.replace(firstLetter, newFirstLetter);
                if(codeStateTable.containsKey(codeState)){
                     stateName = codeStateTable.get(codeState);
                }else{
                    stateName = "Область визначити не вдалося";
                }
            }
            regCountry = regCountry + ", " + stateName;
        }
        txtRegC.setText(regCountry);
    }

    @Override
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mOffenderPhoto != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SendActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 199);
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


    @Override
    protected void onDestroy() {
        //Glide.with(getApplicationContext()).pauseRequests();
        super.onDestroy();
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

