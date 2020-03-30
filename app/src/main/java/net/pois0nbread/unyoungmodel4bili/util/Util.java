package net.pois0nbread.unyoungmodel4bili.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : Util
 *     version: 1.0
 * </pre>
 */

public class Util {

    public static boolean isBgame(String game) {
        if (Settings.getSmartHookMode()) {
            return game.matches(".*bili.*");
        }
        String list = Settings.getHookListMode();
        Log.d("AAAAA", "1" + list);
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
}