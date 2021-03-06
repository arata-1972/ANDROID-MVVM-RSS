package com.moon.myreadapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.moon.appframework.common.log.XLog;
import com.moon.myreadapp.ui.fragments.PrefFragment;


/**
 * Created by moon on 15/10/18.
 */
public class PreferenceUtils {


    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor shareEditor;

    private static PreferenceUtils preferenceUtils = null;


    protected PreferenceUtils(Context context){
        sharedPreferences = context.getSharedPreferences(PrefFragment.PREFRENCE_NAME, Context.MODE_PRIVATE);
        shareEditor = sharedPreferences.edit();
    }

    public static PreferenceUtils getInstance(Context context){
        if (preferenceUtils == null) {
            synchronized (PreferenceUtils.class) {
                if (preferenceUtils == null) {
                    preferenceUtils = new PreferenceUtils(context.getApplicationContext());
                }
            }
        }
        return preferenceUtils;
    }

    public String getStringParam(String key){
        return getStringParam(key, "");
    }

    public String getStringParam(String key, String defaultString){
        return sharedPreferences.getString(key, defaultString);
    }

    public void saveParam(String key, String value)
    {
        shareEditor.putString(key,value).commit();
    }

    public boolean getBooleanParam(String key){
        return getBooleanParam(key, false);
    }

    public boolean getBooleanParam(String key, boolean defaultBool){
        return sharedPreferences.getBoolean(key, defaultBool);
    }

    public void saveParam(String key, boolean value){
        shareEditor.putBoolean(key, value).commit();
    }

    public int getIntParam(String key){
        return getIntParam(key, 0);
    }

    public int getIntParam(String key, int defaultInt){
        int size = sharedPreferences.getInt(key, defaultInt);
        XLog.d("INT size:" +size);
        return sharedPreferences.getInt(key, defaultInt);
    }

    public void saveParam(String key, int value){
        XLog.d("INT size save:" +key + "," + value);
        shareEditor.putInt(key, value).commit();
    }

    public long getLongParam(String key){
        return getLongParam(key, 0);
    }

    public long getLongParam(String key, long defaultInt){
        return sharedPreferences.getLong(key, defaultInt);
    }

    public void saveParam(String key, long value){
        shareEditor.putLong(key, value).commit();
    }

    public void removeKey(String key){
        shareEditor.remove(key).commit();
    }
}
