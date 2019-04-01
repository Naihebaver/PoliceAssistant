package naihebaver.policeassistantapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Objects;

import naihebaver.policeassistantapp.Helpers.InternetConnection;
import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

public class StartActivity extends AppCompatActivity {

    private Button mLogin, mRegister;
    private RelativeLayout mParentView;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        setupToolbar("Національна Поліція України");

        mLogin = findViewById(R.id.login);
        mRegister = findViewById(R.id.register);
        mParentView = findViewById(R.id.parent_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }

        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        //}

        if(isGooglePlayServicesAvailable(this)) {
            if (InternetConnection.checkConnection(this)){


                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                        startActivity(intent);
                        //overridePendingTransition(R.anim.slidein, R.anim.slideout);
                        //finish();
                        return;
                    }
                });

            mRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(StartActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.slidein, R.anim.slideout);
                    //finish();
                    return;
                }
            });
        }else UsefulMethods.showMessage(getString(R.string.msg_connection_problem), this);
        }else UsefulMethods.showMessage(getString(R.string.msg_play_services_problem), this);
            //Snackbar.make(mParentView, R.string.msg_play_services_problem, Snackbar.LENGTH_LONG).show();


    }

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public void setupToolbar(String tittle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        mTitle.setText(tittle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }
}
