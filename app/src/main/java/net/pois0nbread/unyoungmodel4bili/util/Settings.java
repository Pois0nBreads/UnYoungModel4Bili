package net.pois0nbread.unyoungmodel4bili.util;

import de.robv.android.xposed.XSharedPreferences;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : Settings
 *     version: 1.0
 * </pre>
 */

public class Settings {
    private static XSharedPreferences xSharedPreferences = null;
    public static XSharedPreferences getSharedPreferences() {
        if (xSharedPreferences == null) {
            xSharedPreferences = new XSharedPreferences("net.pois0nbread.unyoungmodel4bili", MapKeys.Settings.toString());
            xSharedPreferences.makeWorldReadable();
        } else {
            xSharedPreferences.reload();
        }
        return xSharedPreferences;
    }
    public static boolean getEnable() {return getSharedPreferences().getBoolean(MapKeys.Enable.toString(), false);}

    public static boolean getLogoutModeEnable() {return getSharedPreferences().getBoolean(MapKeys.Logout_Mode.toString(), false);}

    public static boolean getLauncherModeEnable() {return getSharedPreferences().getBoolean(MapKeys.Launcher_Mode.toString(), false);}

    public static boolean getSmartHookMode() {return getSharedPreferences().getBoolean(MapKeys.Smart_Hook_Mode.toString(), true);}

    public static String getHookListMode() {return getSharedPreferences().getString(MapKeys.Hook_List.toString(), null);}
}
