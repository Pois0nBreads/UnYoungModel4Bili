package net.pois0nbread.unyoungmodel4bili.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import net.pois0nbread.unyoungmodel4bili.R;
import net.pois0nbread.unyoungmodel4bili.adapter.AppAdapter;
import net.pois0nbread.unyoungmodel4bili.adapter.AppInfo;
import net.pois0nbread.unyoungmodel4bili.adapter.HookListAdapter;
import net.pois0nbread.unyoungmodel4bili.util.MapKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/04/19
 *     desc   : HookSettingActivity
 *     version: 2.0
 * </pre>
 */

public class HookSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mSharedPreferences = MainActivity.mSharedPreferences;
    private Context mContext = this;

    private SwitchCompat mSmartModeSwitch = null;
    private ListView mListView = null;
    private LinearLayout mLinearLayout = null;
    private ListView mDialogListView = null;

    private View mView  = null;
    private AlertDialog mDialog = null;

    private HookListAdapter mHookListAdapter = null;
    private AppAdapter mAppAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hook_setting);
        bindView();
    }

    private void bindView() {
        //bindView
        mSmartModeSwitch = findViewById(R.id.settings_smart_hook_mode_sw);
        mListView = findViewById(R.id.setting_listView);
        mLinearLayout = findViewById(R.id.setting_linear_layout);

        //setCheck
        mSmartModeSwitch.setChecked(mSharedPreferences.getBoolean(MapKeys.Smart_Hook_Mode.toString(), true));
        if (mSharedPreferences.getBoolean(MapKeys.Smart_Hook_Mode.toString(), true))
            mLinearLayout.setVisibility(View.INVISIBLE);
        else mLinearLayout.setVisibility(View.VISIBLE);

        //setListener
        findViewById(R.id.setting_add_btn).setOnClickListener(this);
        findViewById(R.id.setting_clear_btn).setOnClickListener(this);
        mSmartModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSharedPreferences.edit().putBoolean(MapKeys.Smart_Hook_Mode.toString(), isChecked).apply();
            if (isChecked)
                mLinearLayout.setVisibility(View.INVISIBLE);
            else
                mLinearLayout.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
        });

        //setDialogView
        mView = LayoutInflater.from(mContext).inflate(R.layout.apps_dialog_layout, null);
        mDialogListView = mView.findViewById(R.id.dialog_list);
        mAppAdapter = new AppAdapter(mContext, getAllAppInfos());
        mDialogListView.setAdapter(mAppAdapter);
        mDialogListView.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo appInfo = (AppInfo) parent.getItemAtPosition(position);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ApplicationName", appInfo.getAppName());
                jsonObject.put("PackageName", appInfo.getPackageName());
                String jsonString = mSharedPreferences.getString(MapKeys.Hook_List.toString(), null);
                if (jsonString == null || jsonString.equals("")) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    mSharedPreferences.edit().putString(MapKeys.Hook_List.toString(), jsonArray.toString()).apply();
                    mDialog.dismiss();
                    mHookListAdapter = new HookListAdapter(mContext, getListByShare());
                    mListView.setAdapter(mHookListAdapter);
                    Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.getString("PackageName").equals(jsonObject.getString("PackageName"))) {
                        Toast.makeText(mContext, "该应用已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                jsonArray.put(jsonObject);
                mSharedPreferences.edit().putString(MapKeys.Hook_List.toString(), jsonArray.toString()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mDialog.dismiss();
            mHookListAdapter = new HookListAdapter(mContext, getListByShare());
            mListView.setAdapter(mHookListAdapter);
            Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        mDialog = builder.setTitle("选择应用").setView(mView).setPositiveButton("取消", (dialog, which) -> {
        }).create();

        //setListView
        mHookListAdapter = new HookListAdapter(mContext, getListByShare());
        mListView.setAdapter(mHookListAdapter);
        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            JSONObject mJSONObject = (JSONObject) parent.getItemAtPosition(position);
            if (mJSONObject == null) return true;
            String jsonString = mSharedPreferences.getString(MapKeys.Hook_List.toString(),null);
            if (jsonString == null) return true;
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0;i < jsonArray.length();i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.getString("PackageName").equals(mJSONObject.getString("PackageName"))) {
                        jsonArray.remove(i);
                    }
                }
                mSharedPreferences.edit().putString(MapKeys.Hook_List.toString(), jsonArray.toString()).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mHookListAdapter = new HookListAdapter(mContext, getListByShare());
            mListView.setAdapter(mHookListAdapter);
            return true;
        });
    }

    private List<JSONObject> getListByShare() {
        List<JSONObject> list = new ArrayList<>();
        String jsonString = mSharedPreferences.getString(MapKeys.Hook_List.toString(), null);
        if (jsonString == null) return list;
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<AppInfo> getAllAppInfos() {
        List<AppInfo> list = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ResolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : ResolveInfos) {
            String packageName = ri.activityInfo.packageName;
            Drawable icon = ri.loadIcon(packageManager);
            String appName = ri.loadLabel(packageManager).toString();
            AppInfo appInfo = new AppInfo(icon, appName, packageName);
            list.add(appInfo);
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_add_btn:
                mDialog.show();
                break;
            case R.id.setting_clear_btn:
                mSharedPreferences.edit().putString(MapKeys.Hook_List.toString(), "").apply();
                mHookListAdapter = new HookListAdapter(this, getListByShare());
                mListView.setAdapter(mHookListAdapter);
                Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
