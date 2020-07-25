package net.pois0nbread.unyoungmodel4bili.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

import com.bsgamesdk.android.dc.activity.BSGameAntiIndulegnceActivity;

import net.pois0nbread.unyoungmodel4bili.BuildConfig;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfo;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoAdapter;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoUtil;
import net.pois0nbread.unyoungmodel4bili.util.MyClassLoader;
import net.pois0nbread.unyoungmodel4bili.util.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/07/14
 *     desc   : HookImp
 *     version: 2.2.1
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage {

    //Tag
    private static final String TAG = "BSGameSdk Hook : ";
    //实例
    private Object oCallbackListener = null;
    private Object oBSGameSdk = null;
    private SharedPreferences mPreferences = null;
    private Activity mActivity = null;
    private Context mApplicationContext = null;
    //类
    private Class<?> cBSGameSdkError = null; //com.bsgamesdk.android.callbacklistener.BSGameSdkError
    private Class<?> cCallbackListener = null;   //com.bsgamesdk.android.callbacklistener.CallbackListener
    private Class<?> cBSGameSdk = null;         //com.bsgamesdk.android.BSGameSdk

    private MyClassLoader myClassLoader = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam mLoadPackageParam) {

        if (mLoadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod("net.pois0nbread.unyoungmodel4bili.ui.MainActivity", mLoadPackageParam.classLoader, "isHooked", XC_MethodReplacement.returnConstant(true));
            return;
        }

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws ClassNotFoundException {
                mApplicationContext = (Context) param.args[0];

                Settings mSettings = new Settings(mApplicationContext);
                try {
                    if (!mSettings.getEnable() || !mSettings.isBgame(mLoadPackageParam.packageName)) return;
                } catch (Exception e) {
                    return;
                }

                //加载外部dex
                mPreferences = mApplicationContext.getSharedPreferences("info", Context.MODE_PRIVATE);
                try {
                    ApplicationInfo mApplicationInfo = mApplicationContext.getPackageManager().getApplicationInfo("net.pois0nbread.unyoungmodel4bili", 0);
                    myClassLoader = new MyClassLoader(mApplicationInfo.sourceDir, mApplicationContext.getClassLoader());
                } catch (Exception e) {
                    printLog(e);
                }

                printLog("开始Hook packageName = " + mApplicationContext.getPackageName());
                printLog("Hook : 初始化变量");
                try {
                    cBSGameSdkError = myClassLoader.loadClass("com.bsgamesdk.android.callbacklistener.BSGameSdkError");
                    cCallbackListener = myClassLoader.loadClass("com.bsgamesdk.android.callbacklistener.CallbackListener");
                    cBSGameSdk = myClassLoader.loadClass("com.bsgamesdk.android.BSGameSdk");
                } catch (Exception e) {
                    printLog("Hook Error : 初始化变量 错误信息 : " + e);
                    e.printStackTrace();
                }

                if (mSettings.getLogoutModeEnable()) {
                    myClassLoader.addClassName("com.android.data.sdk.Main");
                    myClassLoader.addClassName("com.bsgamesdk.android.dc.DataCollect");
                    printLog("Hook : 屏蔽上下线数据发收");
                }

                if (mSettings.getLauncherModeEnable()) {
                    //添加双亲委派白名单
                    myClassLoader.addClassName("com.bsgamesdk.android.dc.activity.BSGameAntiIndulegnceActivity");

                    //获取mActivity，mPreferences 实列
                    printLog("Hook : 获取mActivity，mPreferences 实列");
                    XposedHelpers.findAndHookConstructor("com.bsgamesdk.android.BSGameSdk", myClassLoader,
                            boolean.class, Activity.class, String.class, String.class, String.class, String.class,
                            myClassLoader.loadClass("com.bsgamesdk.android.callbacklistener.InitCallbackListener"),
                            myClassLoader.loadClass("com.bsgamesdk.android.callbacklistener.ExitCallbackListener"), new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            mActivity = (Activity) param.args[1];
                            try {
                                String AppName = mActivity.getResources().getString(mActivity.getApplicationInfo().labelRes);
                                Toast.makeText(mActivity, "UnYongModel4Bili 运行成功\n当前游戏：" + AppName, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                printLog(e);
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            oBSGameSdk = param.thisObject;
                        }
                    });

                    //拦截BSGameAntiIndulegnceActivity->onCreate
                    printLog("Hook : 拦截BSGameAntiIndulegnceActivity->onCreate");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.dc.activity.BSGameAntiIndulegnceActivity", myClassLoader, "onCreate", Bundle.class, new XC_MethodHook() {

                        AlertDialog alertDialog;
                        @SuppressLint("SimpleDateFormat")
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Activity myLoginActivity = (Activity) param.thisObject;
                            Context dialogContext = new ContextThemeWrapper(myLoginActivity, android.R.style.Theme_Material_Light_Dialog);
                            Context windowContext = new ContextThemeWrapper(myLoginActivity, android.R.style.Theme_Material_Light);

                            UserInfoAdapter mArrayAdapter = new UserInfoAdapter(windowContext, UserInfoUtil.getUserInfoListBySharedPreferences(mPreferences)) {

                                @Override
                                protected void onDeleteListener(String uid) {
                                    UserInfoUtil.removeUserInfoByUID(mPreferences, uid);
                                    changeData(UserInfoUtil.getUserInfoListBySharedPreferences(mPreferences));
                                    Toast.makeText(myLoginActivity, "删除成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                protected void onLoginListener(UserInfo userInfo) {
                                    try {
                                        setDialogShowing(alertDialog, false);
                                        myLoginActivity.finish();
                                        Method m = cCallbackListener.getDeclaredMethod("onSuccess", Bundle.class);
                                        Bundle mBundle = new Bundle();
                                        mBundle.putInt("result", 1);
                                        mBundle.putString("uid", userInfo.getUid());
                                        mBundle.putString("username", userInfo.getUsername());
                                        mBundle.putString("nickname", userInfo.getNickname());
                                        mBundle.putString("access_token", userInfo.getAccess_token());
                                        mBundle.putString("expire_times", userInfo.getExpire_times());
                                        mBundle.putString("refresh_token", userInfo.getRefresh_token());
                                        m.invoke(oCallbackListener, mBundle);
                                        Toast.makeText(myLoginActivity, "正在登录", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            AlertDialog.Builder mBuild = new AlertDialog.Builder(dialogContext);
                            mBuild.setCancelable(false);
                            mBuild.setMessage("\n单击用户名登录， 长按查看详细信息或删除");
                            mBuild.setNeutralButton("关于&设置", (dialog, which) -> {
                                        myLoginActivity.startActivity(myLoginActivity.getPackageManager().getLaunchIntentForPackage("net.pois0nbread.unyoungmodel4bili"));
                                        setDialogShowing(dialog, true);
                                    });
                            mBuild.setPositiveButton("取消登录", (dialog, which) -> {
                                        try {
                                            myLoginActivity.finish();
                                            setDialogShowing(dialog, false);
                                            Toast.makeText(myLoginActivity, "取消登录", Toast.LENGTH_SHORT).show();
                                            Method m = cCallbackListener.getDeclaredMethod("onFailed", cBSGameSdkError);
                                            m.invoke(oCallbackListener, cBSGameSdkError.getConstructor(int.class, String.class).newInstance(6002, "用户取消登录"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                            mBuild.setNegativeButton("添加用户", (dialog, which) -> {
                                        setDialogShowing(dialog, true);
                                        new AlertDialog.Builder(dialogContext)
                                                .setTitle("选择导入方式")
                                                .setMessage("本地导入将自动获取上次在线导入信息\n适合被限制用户登录")
                                                .setCancelable(false)
                                                .setNeutralButton("取消", (dialog1, which1) -> dialog1.dismiss())
                                                .setPositiveButton("本地导入", (dialog1, which1) -> {
                                                    try {
                                                        Class<?> cq = myClassLoader.loadClass("com.bsgamesdk.android.model.q");
                                                        Class<?> cUserParcelable = myClassLoader.loadClass("com.bsgamesdk.android.model.UserParcelable");
                                                        Object oUserParcelable = cq.getDeclaredMethod("c").invoke(cq.getConstructor(Context.class).newInstance(myLoginActivity));
                                                        if (getField(oUserParcelable, cUserParcelable, "access_token") == null ) {
                                                            Toast.makeText(myLoginActivity, "添加用户失败：没有数据", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }
                                                        UserInfo userInfo = new UserInfo();
                                                        userInfo.setUid(String.valueOf((long) getField(oUserParcelable, cUserParcelable, "uid_long")));
                                                        userInfo.setUsername((String) getField(oUserParcelable, cUserParcelable, "nickname"));
                                                        userInfo.setNickname((String) getField(oUserParcelable, cUserParcelable, "nickname"));
                                                        userInfo.setAccess_token((String) getField(oUserParcelable, cUserParcelable, "access_token"));
                                                        userInfo.setExpire_times(String.valueOf((long) getField(oUserParcelable, cUserParcelable, "expire_in")));
                                                        userInfo.setRefresh_token((String) getField(oUserParcelable, cUserParcelable, "refresh_token"));
                                                        userInfo.setLaste_login_time(new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
                                                        userInfo.setLaste_login_time_long(Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date())));
                                                        UserInfoUtil.putUserInfoToSharedPreferences(mPreferences, userInfo);
                                                        Toast.makeText(myLoginActivity, "添加用户成功", Toast.LENGTH_SHORT).show();
                                                        myLoginActivity.recreate();
                                                        printLog("Hook : 拦截官方登陆器数据成功 , UserName = " + getField(oUserParcelable, cUserParcelable, "username"));
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                })
                                                .setNegativeButton("在线导入", (dialog1, which1) -> {
                                                    try {
                                                        myLoginActivity.finish();
                                                        setDialogShowing(dialog, false);
                                                        Toast.makeText(myLoginActivity, "正在添加用户", Toast.LENGTH_SHORT).show();
                                                        Method m = cBSGameSdk.getDeclaredMethod("login", cCallbackListener);
                                                        m.invoke(oBSGameSdk, new Object[]{null});
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                })
                                                .create().show();
                                    });
                            mBuild.setTitle("B服多用户快速切换登陆器");

                            ListView mListView = new ListView(windowContext);
                            mListView.setAdapter(mArrayAdapter);

                            ScrollView mScrollView = new ScrollView(windowContext);
                            mScrollView.setFillViewport(true);
                            View item = mArrayAdapter.getView(0, null, mListView);
                            item.measure(0,0);
                            mScrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (item.getMeasuredHeight() * 1.5)));
                            mScrollView.addView(mListView);

                            RelativeLayout mRelativeLayout = new RelativeLayout(windowContext);
                            mRelativeLayout.addView(mScrollView);

                            mBuild.setView(mRelativeLayout);
                            alertDialog = mBuild.create();
                            alertDialog.show();
                        }
                    });

                    //拦截官方登陆器，调用自定义登陆器
                    printLog("Hook : 拦截官方登陆器，调用自定义登陆器");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.BSGameSdk", myClassLoader, "login", cCallbackListener, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {}

                        @SuppressLint("SimpleDateFormat")
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            if (param.args[0] != null) {
                                oCallbackListener = param.args[0];
                                param.setResult(null);
                                try {
                                    Intent intent = new Intent(mActivity, BSGameAntiIndulegnceActivity.class);
                                    intent.putExtra("isFromXposed", true);
                                    mActivity.startActivity(intent);
                                } catch (Exception e) {
                                    printLog(e);
                                }
                            } else {
                                InvocationHandler handler = (proxy, method, args) -> {
                                    if ("onSuccess".equals(method.getName())) {
                                        Bundle bundle = (Bundle) args[0];
                                        if (bundle.getInt("result", -1) == 1) {
                                            UserInfo userInfo = new UserInfo();
                                            userInfo.setUid(bundle.getString("uid"));
                                            userInfo.setUsername(bundle.getString("username"));
                                            userInfo.setNickname(bundle.getString("nickname"));
                                            userInfo.setAccess_token(bundle.getString("access_token"));
                                            userInfo.setExpire_times(bundle.getString("expire_times"));
                                            userInfo.setRefresh_token(bundle.getString("refresh_token"));
                                            userInfo.setLaste_login_time(new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
                                            userInfo.setLaste_login_time_long(Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date())));
                                            UserInfoUtil.putUserInfoToSharedPreferences(mPreferences, userInfo);
                                            mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "添加用户成功", Toast.LENGTH_SHORT).show());
                                            printLog("Hook : 拦截官方登陆器数据成功 , UserName = " + bundle.getString("username"));
                                        } else {
                                            mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "添加用户失败 result=" + bundle.getString("result"), Toast.LENGTH_SHORT).show());
                                        }
                                    } else {
                                        mActivity.runOnUiThread(() ->  Toast.makeText(mActivity, "添加用户失败", Toast.LENGTH_SHORT).show());
                                    }
                                    Intent intent = new Intent(mActivity, BSGameAntiIndulegnceActivity.class);
                                    intent.putExtra("isFromXposed", true);
                                    mActivity.startActivity(intent);
                                    return null;
                                };
                                param.args[0] = Proxy.newProxyInstance(myClassLoader, new Class[]{cCallbackListener}, handler);
                            }
                        }
                    });

                    //阻止自动登录
                    printLog("Hook : 阻止自动登录");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.activity.WelcomeActivity", myClassLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Activity activity = (Activity) param.thisObject;

                            activity.setResult(2002);
                            activity.finish();

                        }
                    });
                }

                MyClassLoader.replaceClassLoader1(myClassLoader, mApplicationContext.getClassLoader());
                printLog("Hook : 完成Hook");
            }
        });
    }

    private void setDialogShowing(DialogInterface dialog, boolean b) {
        b = !b;
        try {
            Field field = Objects.requireNonNull(dialog.getClass().getSuperclass()).getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (b) dialog.dismiss();
    }

    private static void printLog(String log) {
        XposedBridge.log(TAG + log);
    }

    private static void printLog(Exception e) {
        XposedBridge.log(TAG + e.getMessage());
        e.printStackTrace();
    }

    private static Object getField(Object o, Class c, String name){
        try {
            Field f = c.getDeclaredField(name);
            return f.get(o);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
