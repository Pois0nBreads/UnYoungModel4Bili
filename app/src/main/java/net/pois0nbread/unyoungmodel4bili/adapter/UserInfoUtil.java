package net.pois0nbread.unyoungmodel4bili.adapter;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : UserInfoUtil
 *     version: 1.0
 * </pre>
 */

public class UserInfoUtil {

    public static  final String MapKey = "UserJSON";

    public static List<UserInfo> getUserInfoListBySharedPreferences(SharedPreferences preferences){
        List<UserInfo> userInfos = new ArrayList<>();
        String jsonString = preferences.getString(MapKey, null);
        if (jsonString == null) return null;
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0;i < jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                UserInfo info = new UserInfo();
                info.setUid(jsonObject.getString("uid"));
                info.setUsername(jsonObject.getString("username"));
                info.setNickname(jsonObject.getString("nickname"));
                info.setAccess_token(jsonObject.getString("access_token"));
                info.setExpire_times(jsonObject.getString("expire_times"));
                info.setRefresh_token(jsonObject.getString("refresh_token"));
                info.setLaste_login_time_long(jsonObject.getLong("last_login_time_long"));
                info.setLaste_login_time(jsonObject.getString("last_login_time"));
                userInfos.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  userInfos;
    }

    public  static void putUserInfoToSharedPreferences(SharedPreferences preferences, UserInfo userInfo){
        JSONObject jsonObject = userInfo.toJSONObject();
        String jsonString = preferences.getString(MapKey, null);
        if (jsonString == null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            preferences.edit().putString(MapKey, jsonArray.toString()).commit();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0;i < jsonArray.length();i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.getString("uid").equals(jsonObject.getString("uid"))) {
                    jsonArray.remove(i);
                }
            }
            jsonArray.put(jsonObject);
            preferences.edit().putString(MapKey, jsonArray.toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void removeAllUserInfoToSharedPreferences(SharedPreferences preferences) {
        preferences.edit().clear().commit();
    }

    public static void removeUserInfoByUID(SharedPreferences preferences, String uid) {
        String jsonString = preferences.getString(MapKey, null);
        if (jsonString == null) return;
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0;i < jsonArray.length();i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.getString("uid").equals(uid)) {
                    jsonArray.remove(i);
                }
            }
            preferences.edit().putString(MapKey, jsonArray.toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
