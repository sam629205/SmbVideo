package com.zh.zhvideoplayer.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zh.zhvideoplayer.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adolf Li on 2015/7/21.
 */
public class Preference {

    private static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
    public static void putString(String key, String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void putInt(String key, int value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void putLong(String key, long value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void putStrList(List<String> strList, String tag){
        String result = "";
        for (int i = 0;i<strList.size();i++){
            if (i==strList.size()-1){
                result += strList.get(i) ;
            }else {
                result += strList.get(i) + "," ;
            }
        }
        Preference.putString(tag, result);
    }

    public static List<String> getStrList(String tag){
        List<String> resultList = new ArrayList<>();
        if (!Preference.getString(tag).equals("")){
            String[] mStrs = Preference.getString(tag).split(",");
            for (String mStr:mStrs){
                if (mStr!=null){
                    resultList.add(mStr);
                }
            }
        }
        return resultList;
    }

    public static int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public static int getInt(String key, int default_value) {
        return pref.getInt(key, default_value);
    }

    public static String getString(String key) {
        return pref.getString(key, "");
    }

    public static String getString(String key, String default_value) {
        return pref.getString(key, default_value);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public static long getLong(String key) {
        return pref.getLong(key, 0);
    }


}
