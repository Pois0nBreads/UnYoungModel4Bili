package net.pois0nbread.unyoungmodel4bili.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/04/19
 *     desc   : Settings
 *     version: 2.0
 * </pre>
 */

public class Settings implements SharedPreferences {

    private Context mContext;

    public Settings(Context context) {
        this.mContext = context;
    }

    public boolean getEnable() {
        return getBoolean(MapKeys.Enable.toString(), false);
    }

    public boolean getLogoutModeEnable() {
        return getBoolean(MapKeys.Logout_Mode.toString(), false);
    }

    public boolean getLauncherModeEnable() {
        return getBoolean(MapKeys.Launcher_Mode.toString(), false);
    }

    public boolean getSmartHookMode() {
        return getBoolean(MapKeys.Smart_Hook_Mode.toString(), true);
    }

    public String getHookListMode() {
        return getString(MapKeys.Hook_List.toString(), null);
    }

    public boolean isBgame(String game) {
        if (getSmartHookMode()) {
            return game.matches(".*bili.*");
        }
        String list = getHookListMode();
        if (list == null || list.equals("")) return false;
        boolean isgame = false;
        try {
            JSONArray jsonArray = new JSONArray(list);
            for (int i = 0;i < jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (game.equals(jsonObject.getString("PackageName")))
                    isgame = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isgame;
    }

    private Map<String, ?> getMap() {
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://net.pois0nbread.unyoungmodel4bili.SettingProvider/setting"), null, null, null, null);
        Map<String, Object> map = new HashMap<>();
        if (cursor == null) return map;
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            if (type.equals("boolean")) {
                map.put(name, Boolean.valueOf(value));
            } else if (type.equals("string")) {
                map.put(name, value);
            } else if (type.equals("int")) {
                map.put(name, Integer.parseInt(value));
            } else if (type.equals("long")) {
                map.put(name, Long.parseLong(value));
            } else if (type.equals("float")) {
                map.put(name, Float.parseFloat(value));
            }
        }
        return map;
    }

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<String, Object>(getMap());
    }

    @Override
    public String getString(String key, String defValue) {
        String v = (String) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> v = (Set<String>) getMap().get(key);
        return v != null ? v : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Integer v = (Integer) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Long v = (Long) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Float v = (Float) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Boolean v = (Boolean) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public boolean contains(String key) {
        return getMap().containsKey(key);
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

}
