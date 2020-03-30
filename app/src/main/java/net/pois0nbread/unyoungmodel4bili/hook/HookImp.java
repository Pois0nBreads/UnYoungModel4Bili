package net.pois0nbread.unyoungmodel4bili.hook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import net.pois0nbread.unyoungmodel4bili.BuildConfig;
import net.pois0nbread.unyoungmodel4bili.util.Settings;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfo;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoAdapter;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoUtil;
import net.pois0nbread.unyoungmodel4bili.util.Util;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/03/30
 *     desc   : HookImp
 *     version: 1.1
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage {

    //Tag
    private final String TAG = "BSGameSdk Hook : ";
    private final String SDK_Version = "2.7.0";
    //实列
    private SharedPreferences mPreferences = null;
    private Activity mActivity = null;
    private Object oBSGameSdkError = null;
    //类
    private Class<?> cBSGameSdkError = null; //com.bsgamesdk.android.callbacklistener.BSGameSdkError
    private Class<?> cCallbackListener = null;   //com.bsgamesdk.android.callbacklistener.CallbackListener
    private Class<?> cBSGameSdk = null;         //com.bsgamesdk.android.BSGameSdk

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam mLoadPackageParam) {

        if (mLoadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod("net.pois0nbread.unyoungmodel4bili.ui.MainActivity" ,mLoadPackageParam.classLoader, "isHooked", XC_MethodReplacement.returnConstant(true));
            return;
        }

        if (!Settings.getEnable()) return;

        if (!Util.isBgame(mLoadPackageParam.packageName)) return;

        printLog("开始Hook packageName = " + mLoadPackageParam.packageName);
        String SDKVersion = getSDKVersion(mLoadPackageParam.classLoader);
        printLog("SDK Version is " + SDKVersion);
        if (SDKVersion.equals(SDK_Version)) {
            printLog("SDK识别状态 : 正常");
        } else {
            printLog("SDK识别状态 : 不是适配的版本");
        }

        printLog("Hook : 初始化变量");
        try {
            cBSGameSdkError = XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.BSGameSdkError", mLoadPackageParam.classLoader);
            cCallbackListener = XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.CallbackListener", mLoadPackageParam.classLoader);
            cBSGameSdk = XposedHelpers.findClass("com.bsgamesdk.android.BSGameSdk", mLoadPackageParam.classLoader);
            oBSGameSdkError = cBSGameSdkError.getConstructor(int.class, String.class).newInstance(new Object[]{6002, "用户取消登录"});
        } catch (Exception e) {
            printLog("Hook Error : 初始化变量 错误信息 : ");
            e.printStackTrace();
        }

        if (Settings.getLogoutModeEnable()) {
            try {
                printLog("Hook : 屏蔽上下线数据发收");
                XC_MethodHook xc_methodHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                };
                XposedHelpers.findAndHookConstructor("com.android.data.sdk.Main", mLoadPackageParam.classLoader, XposedHelpers.findClass("com.android.data.sdk.PreDefined", mLoadPackageParam.classLoader), xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "appDestroy", Activity.class, xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "appOffline", Activity.class, xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "appOnline", Activity.class, xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "dCInit", Activity.class, XposedHelpers.findClass("com.android.data.sdk.domain.model.DataParamsModel", mLoadPackageParam.classLoader), String.class, xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "readChannelId", Activity.class, XposedHelpers.findClass("com.android.data.sdk.domain.model.DataUpModel", mLoadPackageParam.classLoader), xc_methodHook);
                XposedHelpers.findAndHookMethod("com.android.data.sdk.Main", mLoadPackageParam.classLoader, "stop", Activity.class, xc_methodHook);
            } catch (Exception e) {
                printLog("Hook Error : 屏蔽上下线数据发收 错误信息 : ");
                e.printStackTrace();
            }
        }

        if (Settings.getLauncherModeEnable()) {
            printLog("Hook : 获取mActivity，mPreferences 实列");
            XposedHelpers.findAndHookMethod("com.bsgamesdk.android.BSGameSdk", mLoadPackageParam.classLoader, "initialize", boolean.class, Activity.class, String.class, String.class, String.class, String.class, XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.InitCallbackListener", mLoadPackageParam.classLoader), XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.ExitCallbackListener", mLoadPackageParam.classLoader), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    mActivity = (Activity) param.args[1];
                    mPreferences = mActivity.getSharedPreferences("info", Context.MODE_PRIVATE);
                    String AppName = "";
                    try {
                        PackageManager packageManager = mActivity.getPackageManager();
                        PackageInfo packageInfo = packageManager.getPackageInfo(
                                mActivity.getPackageName(), 0);
                        int labelRes = packageInfo.applicationInfo.labelRes;
                        AppName = mActivity.getResources().getString(labelRes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(mActivity, "UnYongModel4Bili 运行成功\n当前游戏：" + AppName, Toast.LENGTH_LONG).show();
                }
            });

            printLog("Hook : 拦截官方登陆器，调用自定义登陆器");
            XposedHelpers.findAndHookMethod("com.bsgamesdk.android.BSGameSdk", mLoadPackageParam.classLoader, "login", cCallbackListener, new XC_MethodHook() {
                AlertDialog alertDialog;
                Object object;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args[0] != null) {
                        object = param.args[0];
                        param.setResult(null);

                        AlertDialog.Builder mBulid = new AlertDialog.Builder(mActivity)
                                .setCancelable(false)
                                .setMessage("\n单击用户名登录， 长按查看详细信息或删除")
                                .setNeutralButton("关于&设置", (dialog, which) -> {
                                    mActivity.startActivity(mActivity.getPackageManager().getLaunchIntentForPackage("net.pois0nbread.unyoungmodel4bili"));
                                    setDialogShowing(dialog, true);
                                })
                                .setPositiveButton("取消登录", (dialog, which) -> {
                                    try {
                                        Method m = cCallbackListener.getDeclaredMethod("onFailed", cBSGameSdkError);
                                        m.invoke(param.args[0], new Object[]{oBSGameSdkError});
                                        Toast.makeText(mActivity, "取消登录", Toast.LENGTH_SHORT).show();
                                        setDialogShowing(dialog, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                })
                                .setNegativeButton("使用官方登录器", (dialog, which) -> {
                                    try {
                                        Method m = cBSGameSdk.getDeclaredMethod("login", cCallbackListener);
                                        m.invoke(param.thisObject, new Object[]{null});
                                        setDialogShowing(dialog, false);
                                        Toast.makeText(mActivity, "正在使用官方登录器", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });

                        if (SDKVersion.equals(SDK_Version)) {
                            mBulid.setTitle("B服多用户快速切换登陆器（SDK识别正常 V" + SDKVersion + "）");
                        } else {
                            mBulid.setTitle("B服多用户快速切换登陆器（⚠ SDK不是适配的版本 V" + SDKVersion + "）");
                        }

                        ListView mListView = new ListView(mActivity);

                        UserInfoAdapter mArrayAdapter = new UserInfoAdapter(mActivity, UserInfoUtil.getUserInfoListBySharedPreferences(mPreferences)) {

                            @Override
                            protected void onDeleteListener(String uid) {
                                UserInfoUtil.removeUserInfoByUID(mPreferences, uid);
                                changeData(UserInfoUtil.getUserInfoListBySharedPreferences(mPreferences));
                                Toast.makeText(mActivity, "删除成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            protected void onLoginListener(UserInfo userInfo) {
                                try {
                                    Method m = cCallbackListener.getDeclaredMethod("onSuccess", Bundle.class);
                                    Bundle mBundle = new Bundle();
                                    mBundle.putInt("result", 1);
                                    mBundle.putString("uid", userInfo.getUid());
                                    mBundle.putString("username", userInfo.getUsername());
                                    mBundle.putString("nickname", userInfo.getNickname());
                                    mBundle.putString("access_token", userInfo.getAccess_token());
                                    mBundle.putString("expire_times", userInfo.getExpire_times());
                                    mBundle.putString("refresh_token", userInfo.getRefresh_token());
                                    m.invoke(param.args[0], new Object[]{mBundle});
                                    alertDialog.dismiss();
                                    Toast.makeText(mActivity, "正在登录", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        mListView.setAdapter(mArrayAdapter);
                        ScrollView mScrollView = new ScrollView(mActivity);
                        mScrollView.setFillViewport(true);
                        mScrollView.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, 350));
                        mScrollView.addView(mListView);
                        RelativeLayout mRelativeLayout = new RelativeLayout(mActivity);
                        mRelativeLayout.addView(mScrollView);
                        mBulid.setView(mRelativeLayout);
                        alertDialog = mBulid.create();
                        alertDialog.show();
                    } else {
                        param.args[0] = object;
                    }
                }
            });

            printLog("Hook : 拦截官方登陆器数据");
            XposedHelpers.findAndHookMethod("com.bsgamesdk.android.j", mLoadPackageParam.classLoader, "run", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Field b = XposedHelpers.findField(XposedHelpers.findClass("com.bsgamesdk.android.j", mLoadPackageParam.classLoader), "b");
                        b.setAccessible(true);
                        Method c = cBSGameSdk.getDeclaredMethod("c", cBSGameSdk);
                        c.setAccessible(true);
                        JSONObject jsonObject = (JSONObject) c.invoke(null, b.get(param.thisObject));
                        if (jsonObject.getString("result").equals("1")) {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setUid(jsonObject.getString("uid"));
                            userInfo.setUsername(jsonObject.getString("username"));
                            userInfo.setNickname(jsonObject.getString("nickname"));
                            userInfo.setAccess_token(jsonObject.getString("access_token"));
                            userInfo.setExpire_times(jsonObject.getString("expire_times"));
                            userInfo.setRefresh_token(jsonObject.getString("refresh_token"));
                            userInfo.setLaste_login_time(new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
                            userInfo.setLaste_login_time_long(Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(new Date())));
                            UserInfoUtil.putUserInfoToSharedPreferences(mPreferences, userInfo);
                            printLog("Hook : 拦截官方登陆器数据成功 , UserName = " + jsonObject.getString("username"));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            printLog("Hook : 屏蔽官方登陆器快速登陆");
            XposedHelpers.findAndHookMethod("com.bsgamesdk.android.activity.Login_RegActivity", mLoadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    ((Context) param.thisObject).getSharedPreferences("login", 0).edit().clear().apply();
                }
            });
        }
        printLog("Hook : 完成Hook");
    }

    private String getSDKVersion(ClassLoader classLoader) {
        try {
            Class<?> c = XposedHelpers.findClass("com.bsgamesdk.android.t", classLoader);
            Method m = c.getDeclaredMethod("getSDK_Version");
            return (String) m.invoke(c.newInstance());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return "null";
    }

    private void setDialogShowing(DialogInterface dialog, boolean b) {
        b = !b;
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (b) dialog.dismiss();
    }

    private void printLog(String log) {
        XposedBridge.log(TAG + log);
    }
}
