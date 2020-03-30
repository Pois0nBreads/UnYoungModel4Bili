package net.pois0nbread.unyoungmodel4bili.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import net.pois0nbread.unyoungmodel4bili.BuildConfig;
import net.pois0nbread.unyoungmodel4bili.R;
import net.pois0nbread.unyoungmodel4bili.util.MapKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/04/19
 *     desc   : MainActivity
 *     version: 2.0
 * </pre>
 */

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    private Context mContext = this;

    private final String updateAdress = "https://pois0nbreads.github.io/Breads/unyoungmodel4bili.json";

    public static SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(MapKeys.Settings.toString(), Context.MODE_PRIVATE);
        bindView();
        if (!isHooked()) {
            new AlertDialog.Builder(mContext)
                    .setTitle("模块未激活或未安装Xposed框架")
                    .setMessage("请先激活模块或安装Xposed框架后再运行软件")
                    .setNegativeButton("确认", (dialog, which) -> {})
                    .create().show();
        }
        if (mSharedPreferences.getBoolean(MapKeys.First_Open.toString(), true)) {
            new AlertDialog.Builder(mContext)
                    .setTitle("用户须知")
                    .setMessage("重要的事情：\n1.这是一个Xposed插件，需要Xposed环境\n2.请尽可能不要在非正常方式登陆时氪金\n3.这是免费软件，请不要在任何地方付费\n4.本软件遵循GNU协议，请勿用于商业用途")
                    .setPositiveButton("不再提示", (dialog, which) -> mSharedPreferences.edit().putBoolean(MapKeys.First_Open.toString(), false).apply())
                    .setNegativeButton("关闭", (dialog, which) -> {})
                    .show();
        }
    }

    private void bindView() {
        //findView
        SwitchCompat mEnableSwitch = findViewById(R.id.main_enable_sw);
        SwitchCompat mLauncherSwitch = findViewById(R.id.main_launcher_mode_sw);
        SwitchCompat mLogoutSwitch = findViewById(R.id.main_logout_mode_sw);

        //setCheckByShareSetting
        mEnableSwitch.setChecked(mSharedPreferences.getBoolean(MapKeys.Enable.toString(), false));
        mLauncherSwitch.setChecked(mSharedPreferences.getBoolean(MapKeys.Launcher_Mode.toString(), false));
        mLogoutSwitch.setChecked(mSharedPreferences.getBoolean(MapKeys.Logout_Mode.toString(), false));

        //bindListener
        mEnableSwitch.setOnCheckedChangeListener(this);
        mLauncherSwitch.setOnCheckedChangeListener(this);
        mLogoutSwitch.setOnCheckedChangeListener(this);
        findViewById(R.id.main_hook_mode_btn).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HookSettingActivity.class)));
        findViewById(R.id.main_qq_group_btn).setOnClickListener(v -> joinQQGroup("9UitjO-Id4O5Sj-wfbR3icMb76XcAQ57"));
        findViewById(R.id.main_check_update_button).setOnClickListener(v -> checkUpdate());
        findViewById(R.id.main_github_button).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pois0nBreads/UnYoungModel4Bili"))));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.main_enable_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Enable.toString(), isChecked).apply();
                Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_launcher_mode_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Launcher_Mode.toString(), isChecked).apply();
                Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_logout_mode_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Logout_Mode.toString(), isChecked).apply();
                Toast.makeText(mContext, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "未安装手Q或安装的版本不支持\nQQ群：1062440816", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checking = false;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject jsonObject = new JSONObject(msg.getData().getString("json"));
                double version_code = jsonObject.getDouble("version_code");
                String last_version = jsonObject.getString("last_version");
                String update_date = jsonObject.getString("update_date");
                String update_text = jsonObject.getString("update_text");
                String version_type = jsonObject.getString("version_type");
                JSONArray downloadURLs = jsonObject.getJSONArray("downloadURLs");
                JSONArray update_log = jsonObject.getJSONArray("update_log");

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
                mBuilder.setTitle("检查更新");
                mBuilder.setMessage("最新版本号：" + last_version + " " + version_type + "\n更新日期：" + update_date + "\n\n更新内容：\n" + update_text);
                mBuilder.setPositiveButton("关闭", (dialog, which) -> {});
                mBuilder.setNegativeButton("下载更新", (dialog, which) -> {

                    if (version_code <= BuildConfig.VERSION_CODE) {
                        Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> download_URLs = new ArrayList<>();
                    ScrollView mScrollView = new ScrollView(mContext);
                    ListView mListView = new ListView(mContext);
                    RelativeLayout mRelativeLayout = new RelativeLayout(mContext);
                    ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);

                    for (int i = 0;i < downloadURLs.length();i++) {
                        try {
                            JSONObject js = downloadURLs.getJSONObject(i);
                            String download_Info = js.getString("download_Info");
                            download_URLs.add(js.getString("download_URL"));
                            mArrayAdapter.add(download_Info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    mListView.setOnItemClickListener((parent, view, position, id) -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_URLs.get(position))));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mListView.setAdapter(mArrayAdapter);

                    mScrollView.setFillViewport(true);
                    mScrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350));
                    mScrollView.addView(mListView);
                    mRelativeLayout.addView(mScrollView);

                    AlertDialog.Builder mDownloadDialog = new AlertDialog.Builder(mContext);
                    mDownloadDialog.setTitle("选择下载渠道");
                    mDownloadDialog.setNegativeButton("取消", (dialog1, which1) -> dialog1.dismiss());
                    mDownloadDialog.setView(mRelativeLayout);
                    mDownloadDialog.show();
                });
                mBuilder.setNeutralButton("更新日志", (dialog, which) -> {
                    AlertDialog.Builder updateLogDialog = new AlertDialog.Builder(mContext);
                    StringBuilder updateLog = new StringBuilder();
                    for (int i = 0;i < update_log.length();i++) {
                        try {
                            updateLog.append(update_log.getString(i)).append("\n\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    updateLogDialog.setTitle("更新日志");
                    updateLogDialog.setMessage(updateLog.toString());
                    updateLogDialog.show();
                });
                mBuilder.create().show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "检查更新出错", Toast.LENGTH_SHORT).show();
            } finally {
                checking = false;
            }
        }
    };

    private void checkUpdate() {
        if (checking) {
            Toast.makeText(mContext, "别点那么快", Toast.LENGTH_SHORT).show();
            return;
        }
        checking = true;
        new Thread(() -> {
            StringBuilder get = new StringBuilder();
            try {
                URL url = new URL(updateAdress);
                URLConnection conn = url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String buf;
                while ((buf = br.readLine()) != null) {
                    get.append(buf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("json", get.toString());
            message.setData(bundle);
            mHandler.sendMessage(message);
        }).start();
    }

        private boolean isHooked () {
            Log.i("isHooked", "MG4");
            return false;
        }
    }
