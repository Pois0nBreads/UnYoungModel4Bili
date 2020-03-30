package net.pois0nbread.unyoungmodel4bili.hook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import net.pois0nbread.unyoungmodel4bili.util.MapKeys;

import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/04/19
 *     desc   : SettingProvider
 *     version: 2.0
 * </pre>
 */

public class SettingProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"name", "value", "type"});
        Map<String, ?> map = getContext().getSharedPreferences(MapKeys.Settings.toString(), Context.MODE_PRIVATE).getAll();
        Set<String> krySet = map.keySet();
        for (String key : krySet) {
            Object[] rows = new Object[3];
            rows[0] = key;
            rows[1] = map.get(key);
            if (rows[1] instanceof Boolean) {
                rows[2]="boolean";
            }else if (rows[1] instanceof String) {
                rows[2]="string";
            }else if (rows[1] instanceof Integer) {
                rows[2]="int";
            }else if (rows[1] instanceof Long) {
                rows[2]="long";
            }else if (rows[1] instanceof Float) {
                rows[2]="float";
            }
            cursor.addRow(rows);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


}
