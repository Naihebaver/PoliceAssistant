package naihebaver.policeassistantapp.Helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsefulMethods extends AppCompatActivity {

    private Context context;


    public UsefulMethods(Context context){
        this.context=context;
    }

    public static void showMessage(String message, Context context) {

        Toast.makeText(context,message,Toast.LENGTH_LONG).show();

    }


    public static boolean isUA(String str){

        try{

            if(str.length() > 10) return false;
            String REGEX = "[A-Z]{2}\\s\\d{4}\\s[A-Z]{2}";
            Pattern r = Pattern.compile(REGEX);
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "UA");
                return true;
            }

        }catch (Exception e){
            return false;
        }

    }

    public static boolean isSK(String str){

        try{

            if(str.length() > 8) return false;
            String REGEX = "[A-Z]{2}\\s\\d{3}[A-Z]{2}";
            Pattern r = Pattern.compile(REGEX);
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "SK");
                return true;
            }

        }catch (Exception e){
            return false;
        }
    }

    public static boolean isPL(String str){

        try{

            if(str.length() > 9) return false;
            String REGEX = "[A-Z]{2,3}\\s[0-9A-Z]{4,5}";
            Pattern r = Pattern.compile(REGEX);
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "PL");
                return true;
            }

        }catch (Exception e){
            return false;
        }
    }

    public static boolean isHU(String str){

        try{

            if(str.length() != 7) return false;
            String REGEX = "[A-Z]{3}[-]\\d{3}";
            Pattern r = Pattern.compile(REGEX);
            Matcher m = r.matcher(str);
            if(!m.find()) return false;
            else {
                Log.e("radar_1 = " , "HU");
                return true;
            }

        }catch (Exception e){
            return false;
        }
    }



}
