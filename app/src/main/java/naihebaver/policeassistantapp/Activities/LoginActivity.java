package naihebaver.policeassistantapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import naihebaver.policeassistantapp.Models.User;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userPhone,userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private ImageView userPhoto;

    List<User> mUsersList = new ArrayList<>();
    SharedPreferencesHelper mSharedPrefsHelper;

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mUsersRef = mDatabase.getReference("Users/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupToolbar("Вхід в аккаунт");

        mSharedPrefsHelper = new SharedPreferencesHelper(this);

        getUsersList();


        userPhone = findViewById(R.id.logPhone);
        userPassword = findViewById(R.id.logPassword);
        btnLogin = findViewById(R.id.logBtn);
        loginProgress = findViewById(R.id.logProgressBar);
        userPhoto = findViewById(R.id.logUserPhoto);

        userPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPhone.setText("+380");
                userPhone.setSelection(userPhone.getText().length());
                //v.setOnClickListener(null);
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility(View.VISIBLE);
                String phone = userPhone.getText().toString().trim();
                String password = userPassword.getText().toString().trim();
                password = md5(password);

                if (phone.isEmpty() || password.isEmpty()) {
                    showMessage("Будь ласка, введіть всі дані!") ;
                    loginProgress.setVisibility(View.INVISIBLE);
                }
                else
                {
                    signIn(phone,password);
                }

            }
        });
    }

    private void getUsersList(){
        mUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsersList.clear();
                for (DataSnapshot dt: dataSnapshot.getChildren()) {

                    User user = dt.getValue(User.class);
                    if(user != null) mUsersList.add(user);
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
        if(mSharedPrefsHelper.getUserPhoto() != null){
            userPhoto.setImageBitmap(decodeBase64(mSharedPrefsHelper.getUserPhoto()));
        }
    }

    private void signIn(String phone, String password) {
        if(mUsersList != null){
            for (User u: mUsersList) {
                if(u.getPhone().equals(phone) && u.getPassword().equals(password)){

                    mSharedPrefsHelper.setKey(u.getKey());

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    return;

                }

            }

            showMessage("Неправильний номер телефону або пароль...");
            loginProgress.setVisibility(View.INVISIBLE);
        }

    }

    // simple method to show toast message
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    public static String md5(final String s){

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

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
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
