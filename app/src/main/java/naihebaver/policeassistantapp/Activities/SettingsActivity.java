package naihebaver.policeassistantapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Objects;

import naihebaver.policeassistantapp.R;
import naihebaver.policeassistantapp.Helpers.SharedPreferencesHelper;
import naihebaver.policeassistantapp.Helpers.UsefulMethods;

public class SettingsActivity extends AppCompatActivity {

    private Button mSaveSettingsBtn;
    private EditText mNotifyRadius;
    private RadioButton mForeignModeOff, mForeignModeOnn;
    SharedPreferencesHelper mSharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar("Налаштування");

        //mMinimumMode = findViewById(R.id.radio_minimum_mode);
        ///mNormalMode = findViewById(R.id.radio_normal_mode);
        //mMaximumMode = findViewById(R.id.radio_maximum_mode);

        mForeignModeOff = findViewById(R.id.radio_foreign_off);
        mForeignModeOnn = findViewById(R.id.radio_foreign_onn);

        mSaveSettingsBtn = findViewById(R.id.settingsBtn);
        mSharedPrefsHelper = new SharedPreferencesHelper(this);

        //RadioGroup radioGroup = findViewById(R.id.radio_group);
        RadioGroup radioGroup2 = findViewById(R.id.radio_group2);

//        int workingMode = mSharedPrefsHelper.getWorkingMode();
//        switch (workingMode) {
//            case 1:
//                mMinimumMode.setChecked(true);
//                break;
//            case 2:
//                mNormalMode.setChecked(true);
//                break;
//            case 3:
//                mMaximumMode.setChecked(true);
//                break;
//        }
//
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.radio_minimum_mode:
//                        mSharedPrefsHelper.setWorkingMode(1);
//                        break;
//                    case R.id.radio_normal_mode:
//                        mSharedPrefsHelper.setWorkingMode(2);
//                        break;
//                    case R.id.radio_maximum_mode:
//                        mSharedPrefsHelper.setWorkingMode(3);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });

        int foreignMode = mSharedPrefsHelper.getForeignMode();
        switch (foreignMode) {
            case 0:
                mForeignModeOff.setChecked(true);
                break;
            case 1:
                mForeignModeOnn.setChecked(true);
                break;
        }

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_foreign_off:
                        mSharedPrefsHelper.setForeignMode(0);
                        break;
                    case R.id.radio_foreign_onn:
                        mSharedPrefsHelper.setForeignMode(1);
                        break;
                    default:
                        break;
                }
            }
        });











        mNotifyRadius = findViewById(R.id.notify_radius);
        mNotifyRadius.setText(mSharedPrefsHelper.getNotifyRadius().toString());
//        mNotifyRadius.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mNotifyRadius.setText("");
//            }
//        });

        mSaveSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer NotifyRadius = Integer.valueOf(mNotifyRadius.getText().toString());
                mSharedPrefsHelper.setNotifyRadius(NotifyRadius);
                UsefulMethods.showMessage("Налаштування успішно збережені!", getApplicationContext());
            }
        });
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
