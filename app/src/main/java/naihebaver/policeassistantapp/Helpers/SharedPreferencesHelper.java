package naihebaver.policeassistantapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    Context context;
    private static final String APP_PREFERENCES = "config";
    private static final String APP_PREFERENCES_DATE = "date";
    private static final String APP_PREFERENCES_TIME = "time";
    private static final String APP_PREFERENCES_KEY = "key";
    private static final String APP_PREFERENCES_USER_NAME = "user_name";
    private static final String APP_PREFERENCES_USER_PHONE = "user_phone";
    private static final String APP_PREFERENCES_USER_STATE = "user_state";
    private static final String APP_PREFERENCES_USER_PHOTO = "user_photo";

    private static final String APP_PREFERENCES_NOTIFY_RADIUS = "notify_radius";
    private static final String APP_PREFERENCES_WORKING_MODE = "working_mode";
    private static final String APP_PREFERENCES_FOREIGN_MODE = "foreign_mode";
    private static final String APP_PREFERENCES_SERVICE_WORKING = "service_working";

    private static final String APP_PREFERENCES_COLLEAGUE_PHONE = "colleague_phone";

    private SharedPreferences mSettings;

    public SharedPreferencesHelper(Context context) {
        this.context = context;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String  getDate(){
        return mSettings.getString(APP_PREFERENCES_DATE,"0");
    }

    public void setDate(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_DATE, id);
        editor.apply();
    }

    public String getTime(){
        return mSettings.getString(APP_PREFERENCES_TIME,"0");
    }

    public void setTime(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_TIME, id);
        editor.apply();
    }

    public String  getKey(){
        return mSettings.getString(APP_PREFERENCES_KEY,"0");
    }

    public void setKey(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_KEY, id);
        editor.apply();
    }

    public String getUserName(){
        return mSettings.getString(APP_PREFERENCES_USER_NAME,"0");
    }

    public void setUserName(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_USER_NAME, id);
        editor.apply();
    }

    public String getUserPhone(){
        return mSettings.getString(APP_PREFERENCES_USER_PHONE,"0");
    }

    public void setUserPhone(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_USER_PHONE, id);
        editor.apply();
    }

    public String getUserState(){
        return mSettings.getString(APP_PREFERENCES_USER_STATE,"0");
    }

    public void setUserState(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_USER_STATE, id);
        editor.apply();
    }


    public String getUserPhoto(){
        return mSettings.getString(APP_PREFERENCES_USER_PHOTO, null);
    }

    public void setUserPhoto(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_USER_PHOTO, id);
        editor.apply();
    }



    public Integer getNotifyRadius(){
        return mSettings.getInt(APP_PREFERENCES_NOTIFY_RADIUS,3);
    }

    public void setNotifyRadius(Integer id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_NOTIFY_RADIUS, id);
        editor.apply();
    }

    public Integer getForeignMode(){
        return mSettings.getInt(APP_PREFERENCES_FOREIGN_MODE,0);
    }

    public void setForeignMode(Integer id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_FOREIGN_MODE, id);
        editor.apply();
    }

    public Integer getWorkingMode(){
        return mSettings.getInt(APP_PREFERENCES_WORKING_MODE,2);
    }

    public void setWorkingMode(Integer id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_WORKING_MODE, id);
        editor.apply();
    }

    public Integer getServiceWorking(){
        return mSettings.getInt(APP_PREFERENCES_SERVICE_WORKING,0);
    }

    public void setServiceWorking(Integer id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_SERVICE_WORKING, id);
        editor.apply();
    }






    public String getColleaguePhone(){
        return mSettings.getString(APP_PREFERENCES_COLLEAGUE_PHONE,null);
    }

    public void setColleaguePhone(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_COLLEAGUE_PHONE, id);
        editor.apply();
    }





    public void clear(){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.clear();
        editor.apply();
    }
}
