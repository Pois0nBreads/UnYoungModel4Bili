package net.pois0nbread.unyoungmodel4bili.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.pois0nbread.unyoungmodel4bili.R;
import net.pois0nbread.unyoungmodel4bili.util.MapKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : MainActivity
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Switch mEnableSwitch = null;
    private Switch mLauncherSwitch = null;
    private Switch mLogoutSwitch = null;

    private final String updateAdress = "https://pois0nbreads.github.io/Breads/unyoungmodel4bili.json";

    private AlertDialog mPy_Pay_alertDialog = null;

    private SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(MapKeys.Settings.toString(), Context.MODE_WORLD_WRITEABLE);
        bindView();
        if (!isHooked()) {
            new AlertDialog.Builder(this)
                    .setTitle("模块未激活或未安装Xposed框架")
                    .setMessage("请先激活模块或安装Xposed框架后再运行软件")
                    .setNegativeButton("确认", (dialog, which) -> {
                    }).create().show();
            return;
        }
        if (mSharedPreferences.getBoolean(MapKeys.First_Open.toString(), true)) {
            new AlertDialog.Builder(this)
                    .setTitle("来自作者的留言 ⁄(⁄ ⁄•⁄ω⁄•⁄ ⁄)⁄")
                    .setMessage("制作这个软件花费我不少时间，如果你喜欢这个项目，可以赞助我买瓶可乐\n⊙▽⊙")
                    .setNegativeButton("给作者打钱", (dialog, which) -> mPy_Pay_alertDialog.show())
                    .setPositiveButton("下次再说", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            mSharedPreferences.edit().putBoolean(MapKeys.First_Open.toString(), false).commit();
        }
        checkUpdate(updateAdress);
    }

    private void bindView() {
        //findView
        mEnableSwitch = findViewById(R.id.main_enable_sw);
        mLauncherSwitch = findViewById(R.id.main_launcher_mode_sw);
        mLogoutSwitch = findViewById(R.id.main_logout_mode_sw);

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
        findViewById(R.id.main_py_pay_button).setOnClickListener(v -> mPy_Pay_alertDialog.show());
        findViewById(R.id.main_check_update_button).setOnClickListener(v -> checkUpdate(updateAdress));
        findViewById(R.id.main_github_button).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pois0nBreads/UnYoungModel4Bili"))));

        //bindDialogView
        View view = View.inflate(this, R.layout.dialog_layout, null);
        view.findViewById(R.id.dialog_button1).setOnClickListener(this);
        view.findViewById(R.id.dialog_button2).setOnClickListener(this);
        view.findViewById(R.id.dialog_button3).setOnClickListener(this);
        mPy_Pay_alertDialog = new AlertDialog.Builder(this).setView(view).setTitle("请选择赞助渠道 ⊙▽⊙").create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.main_enable_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Enable.toString(), isChecked).commit();
                Toast.makeText(this, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_launcher_mode_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Launcher_Mode.toString(), isChecked).commit();
                Toast.makeText(this, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_logout_mode_sw:
                mSharedPreferences.edit().putBoolean(MapKeys.Logout_Mode.toString(), isChecked).commit();
                Toast.makeText(this, "重启游戏生效", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_button1:
                String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ffkx00694rmzfhta8chwcc3c%3F_s" +
                        "%3Dweb-other&_t=1472443966571#Intent;" +
                        "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                try {
                    getPackageManager().getApplicationInfo("com.eg.android.AlipayGphone", PackageManager.GET_UNINSTALLED_PACKAGES);
                    Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "您似乎没有安装支付宝", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.dialog_button2:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/pois0nbread")));
                break;
            case R.id.dialog_button3:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pois0nbreads.github.io/Breads/")));
                break;
        }
        mPy_Pay_alertDialog.dismiss();
    }

    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "未安装手Q或安装的版本不支持", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject jsonObject = new JSONObject(msg.getData().getString("json"));
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                double version_code = jsonObject.getDouble("version_code");
                String last_version = jsonObject.getString("last_version");
                String update_date = jsonObject.getString("update_date");
                String update_text = jsonObject.getString("update_text");
                String update_url = jsonObject.getString("update_url");
                String version_type = jsonObject.getString("version_type");
                if (version_code <= 1.0) {
                    Toast.makeText(MainActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                    return;
                }
                mBuilder.setTitle("检查更新");
                mBuilder.setMessage("最新版本号：" + last_version + " " + version_type + "\n更新日期：" + update_date + "\n\n更新内容：\n" + update_text);
                mBuilder.setPositiveButton("关闭", (dialog, which) -> {});
                mBuilder.setNegativeButton("下载更新", (dialog, which) ->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(update_url))));
                mBuilder.create().show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "检查更新出错", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void checkUpdate(String adress) {
        new Thread(() -> {
            String get = "";
            try {
                URL url = new URL(adress);
                URLConnection urlconn = url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
                String buf;
                while ((buf = br.readLine()) != null) {
                    get += buf;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("json", get);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }).start();
    }

        private boolean isHooked () {
            return false;
        }
    }
