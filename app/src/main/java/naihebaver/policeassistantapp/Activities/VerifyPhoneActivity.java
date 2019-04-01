package naihebaver.policeassistantapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import naihebaver.policeassistantapp.Models.User;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;

public class VerifyPhoneActivity extends AppCompatActivity {

    ImageView userPhoto;
    StorageReference storageReference;
    private User inSelfUser = null;
    SharedPreferencesHelper mSharedPrefsHelper;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mUsersRef = mDatabase.getReference("Users/");


    private String verificationId;
    private FirebaseAuth mAuth;
    private ProgressBar regProgressBar;
    private EditText regCodeTxt;

    private String name, phone, email, password;
    Uri pickedImgUri, downloadImgUrl;
    String state;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        setupToolbar("Верифікація номера");

        mSharedPrefsHelper = new SharedPreferencesHelper(this);


        mAuth = FirebaseAuth.getInstance();
        regProgressBar = findViewById(R.id.regProgressBar);
        regCodeTxt = findViewById(R.id.regCodeTxt);
        userPhoto = findViewById(R.id.regUserPhoto);

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        state = getIntent().getStringExtra("state");
        if(getIntent().getStringExtra("user_photo") != null){
            pickedImgUri = Uri.parse(getIntent().getStringExtra("user_photo"));
        }

        uploadUserPhoto();

        if(pickedImgUri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            userPhoto.setImageURI(pickedImgUri);
            mSharedPrefsHelper.setUserPhoto(encodeTobase64(bitmap));
        }


        sendVerificationCode(phone);
        findViewById(R.id.regBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = regCodeTxt.getText().toString().trim();

                if (code.isEmpty() || code.length() < 6) {

                   regCodeTxt.setError("Введіть код...");
                   regCodeTxt.requestFocus();
                   return;
                }
                verifyCode(code);

            }
        });
    }


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            createUserAccount(phone, name, email, password, state);

                        } else {
                            Toast.makeText(VerifyPhoneActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String number) {
        regProgressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                regCodeTxt.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };


    private void createUserAccount(String phone, String name, String email, String password, String state) {

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPhone(phone);
        user.setPassword(md5(password));
        user.setState(state);
        if(downloadImgUrl != null){
            user.setUserPhoto(downloadImgUrl.toString());
        }

        String key = mUsersRef.push().getKey();
        user.setKey(key);
        mUsersRef.child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(VerifyPhoneActivity.this, "Ви успішно зареєструвалися!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(VerifyPhoneActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                }else {
                    Toast.makeText(VerifyPhoneActivity.this, "Помилка реєстрації...", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }

    private void uploadUserPhoto() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (pickedImgUri != null) {

                    //final StorageReference ref = storageReference.child("users_photos/"+ UUID.randomUUID().toString());
                    //UploadTask uploadTask = ref.putFile(pickedImgUri);

//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImgUri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();

                    storageReference = FirebaseStorage.getInstance().getReference().child("users_photos").child(UUID.randomUUID().toString());
                    storageReference.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadImgUrl = uri;
                                }
                            });
                        }
                    });


//            storageReference = FirebaseStorage.getInstance().getReference().child("offender_photos").child(UUID.randomUUID().toString());
//            UploadTask uploadTask = storageReference.putBytes(byteArray);
//            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task){
//                    if(!task.isSuccessful()){
//                        //throw task.getException();
//                        //Toast.makeText(OcrCaptureActivity.this, "Помилка обробки зображення...", Toast.LENGTH_SHORT).show();
//                    }
//                    return storageReference.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()){
//                        downloadImgUrl = task.getResult();
//                        //startSendAct(downloadUrl.toString());
//                        //isShowDialog = false;
//                        //dialog.dismiss();
//                    }
//                }
//            });
                }
            }
        });
    }

    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public static final String md5(final String s){

        final String MD5 = "MD5";
        try{

            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDiest[] = digest.digest();

            StringBuilder hasString = new StringBuilder();

            for(byte aMessageDiest : messageDiest){

                String h = Integer.toHexString(0xff & aMessageDiest);

                while (h.length() < 2)
                    h = "0" + h;
                hasString.append(h);
            }

            return hasString.toString();

        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
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
