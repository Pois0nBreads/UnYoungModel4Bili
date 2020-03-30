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
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

import net.pois0nbread.unyoungmodel4bili.BuildConfig;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfo;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoAdapter;
import net.pois0nbread.unyoungmodel4bili.adapter.UserInfoUtil;
import net.pois0nbread.unyoungmodel4bili.util.Settings;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import dalvik.system.DexClassLoader;
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
 *     time   : 2030/04/19
 *     desc   : HookImp
 *     version: 2.0
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage {

    //Tag
    private static final String TAG = "BSGameSdk Hook : ";
    private final String SDK_Version = "2.7.0";
    //实例
    private Settings mSettings = null;
    private XC_MethodHook.MethodHookParam paramBSGameSdk = null;
    private SharedPreferences mPreferences = null;
    private Activity mActivity = null;
    private Context mApplicationContext = null;
    private Object oBSGameSdkError = null;
    //类加载器
    private DexClassLoader dcLoader = null;
    //类
    private Class<?> cBSGameSdkError = null; //com.bsgamesdk.android.callbacklistener.BSGameSdkError
    private Class<?> cCallbackListener = null;   //com.bsgamesdk.android.callbacklistener.CallbackListener
    private Class<?> cBSGameSdk = null;         //com.bsgamesdk.android.BSGameSdk

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam mLoadPackageParam) {

        if (mLoadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            XposedHelpers.findAndHookMethod("net.pois0nbread.unyoungmodel4bili.ui.MainActivity", mLoadPackageParam.classLoader, "isHooked", XC_MethodReplacement.returnConstant(true));
            return;
        }

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                mApplicationContext = (Context) param.args[0];

                mSettings = new Settings(mApplicationContext);
                try {
                    if (!mSettings.getEnable() || !mSettings.isBgame(mLoadPackageParam.packageName)) return;
                } catch (Exception e) {
                    return;
                }

                mPreferences = mApplicationContext.getSharedPreferences("info", Context.MODE_PRIVATE);
                try {
                    ApplicationInfo mApplicationInfo = mApplicationContext.getPackageManager().getApplicationInfo("net.pois0nbread.unyoungmodel4bili", 0);
                    String dexPath = mApplicationInfo.sourceDir;
                    String dexOutputDir = mApplicationContext.getApplicationInfo().dataDir;
                    String libPath = mApplicationInfo.nativeLibraryDir;
                    dcLoader = new DexClassLoader(dexPath, dexOutputDir, libPath, mLoadPackageParam.classLoader);
                } catch (Exception e) {
                    printLog(e.getMessage());
                }

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
                    oBSGameSdkError = cBSGameSdkError.getConstructor(int.class, String.class).newInstance(6002, "用户取消登录");
                } catch (Exception e) {
                    printLog("Hook Error : 初始化变量 错误信息 : ");
                    e.printStackTrace();
                }


                if (mSettings.getLogoutModeEnable()) {
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

                if (mSettings.getLauncherModeEnable()) {

                    printLog("Hook : 获取mActivity，mPreferences 实列");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.BSGameSdk", mLoadPackageParam.classLoader, "initialize", boolean.class, Activity.class, String.class, String.class, String.class, String.class, XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.InitCallbackListener", mLoadPackageParam.classLoader), XposedHelpers.findClass("com.bsgamesdk.android.callbacklistener.ExitCallbackListener", mLoadPackageParam.classLoader), new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {

                            try {
                                mActivity = (Activity) param.args[1];
                                hook(mLoadPackageParam.classLoader);

                                XposedHelpers.findAndHookMethod("net.pois0nbread.unyoungmodel4bili.hook.MyLoginActivity", dcLoader, "onCreate", Bundle.class, new XC_MethodHook() {

                                    AlertDialog alertDialog;
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) {
                                        param.setResult(null);
                                        Activity myLoginActivity = (Activity) param.thisObject;
                                        Context dialogContext = new ContextThemeWrapper(myLoginActivity, android.R.style.Theme_Material_Light_Dialog);
                                        Context windowContext = new ContextThemeWrapper(myLoginActivity, android.R.style.Theme_Material_Light);
                                        try {
                                            dcLoader.loadClass("net.pois0nbread.unyoungmodel4bili.hook.MyLoginActivity").getDeclaredMethod("superOnCreate", Bundle.class).invoke(myLoginActivity, (Bundle) param.args[0]);
                                        } catch (Exception e) {
                                            printLog(e.getMessage());
                                        }
                                        AlertDialog.Builder mBuild = new AlertDialog.Builder(dialogContext)
                                                .setCancelable(false)
                                                .setMessage("\n单击用户名登录， 长按查看详细信息或删除")
                                                .setNeutralButton("关于&设置", (dialog, which) -> {
                                                    myLoginActivity.startActivity(myLoginActivity.getPackageManager().getLaunchIntentForPackage("net.pois0nbread.unyoungmodel4bili"));
                                                    setDialogShowing(dialog, true);
                                                })
                                                .setPositiveButton("取消登录", (dialog, which) -> {
                                                    try {
                                                        myLoginActivity.finish();
                                                        setDialogShowing(dialog, false);
                                                        Toast.makeText(myLoginActivity, "取消登录", Toast.LENGTH_SHORT).show();
                                                        Method m = cCallbackListener.getDeclaredMethod("onFailed", cBSGameSdkError);
                                                        m.invoke(paramBSGameSdk.args[0], oBSGameSdkError);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                })
                                                .setNegativeButton("使用官方登录器", (dialog, which) -> {
                                                    try {
                                                        myLoginActivity.finish();
                                                        setDialogShowing(dialog, false);
                                                        Toast.makeText(myLoginActivity, "正在使用官方登录器", Toast.LENGTH_SHORT).show();
                                                        Method m = cBSGameSdk.getDeclaredMethod("login", cCallbackListener);
                                                        m.invoke(paramBSGameSdk.thisObject, new Object[]{null});
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });

                                        if (SDKVersion.equals(SDK_Version)) mBuild.setTitle("B服多用户快速切换登陆器（SDK识别正常 V" + SDKVersion + "）");
                                        else mBuild.setTitle("B服多用户快速切换登陆器（⚠ SDK不是适配的版本 V" + SDKVersion + "）");

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
                                                    m.invoke(paramBSGameSdk.args[0], mBundle);
                                                    Toast.makeText(myLoginActivity, "正在登录", Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };

                                        ListView mListView = new ListView(windowContext);
                                        mListView.setAdapter(mArrayAdapter);
                                        ScrollView mScrollView = new ScrollView(windowContext);
                                        mScrollView.setFillViewport(true);
                                        mScrollView.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, 350));
                                        mScrollView.addView(mListView);
                                        RelativeLayout mRelativeLayout = new RelativeLayout(windowContext);
                                        mRelativeLayout.addView(mScrollView);
                                        mBuild.setView(mRelativeLayout);
                                        alertDialog = mBuild.create();
                                        alertDialog.show();
                                    }
                                });

                                PackageInfo packageInfo = mApplicationContext.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
                                int labelRes = packageInfo.applicationInfo.labelRes;
                                String AppName = mActivity.getResources().getString(labelRes);
                                Toast.makeText(mActivity, "UnYongModel4Bili 运行成功\n当前游戏：" + AppName, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                printLog(e.getMessage());
                            }
                        }
                    });

                    printLog("Hook : 拦截官方登陆器，调用自定义登陆器");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.BSGameSdk", mLoadPackageParam.classLoader, "login", cCallbackListener, new XC_MethodHook() {
                        Object object;
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            paramBSGameSdk = param;
                            if (param.args[0] != null) {
                                object = param.args[0];
                                param.setResult(null);
                                try {
                                    printLog(dcLoader.loadClass("net.pois0nbread.unyoungmodel4bili.hook.MyLoginActivity").getName());
                                    mActivity.startActivity(new Intent(mActivity, dcLoader.loadClass("net.pois0nbread.unyoungmodel4bili.hook.MyLoginActivity")));
                                } catch (ClassNotFoundException e) {
                                    printLog(e.getMessage());
                                }
                            } else {
                                param.args[0] = object;
                            }
                        }
                    });

                    printLog("Hook : 拦截官方登陆器数据");
                    XposedHelpers.findAndHookMethod("com.bsgamesdk.android.j", mLoadPackageParam.classLoader, "run", new XC_MethodHook() {
                        @SuppressLint("SimpleDateFormat")
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
                            } catch (Exception e) {
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
        });
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

    @SuppressLint("PrivateApi")
    private void hook(ClassLoader loader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Object gDefault;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Field singletonField = loader.loadClass("android.app.ActivityManager").getDeclaredField("IActivityManagerSingleton");
            singletonField.setAccessible(true);
            gDefault = singletonField.get(null);
        } else {
            Field gDefaultField = loader.loadClass("android.app.ActivityManagerNative").getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            gDefault = gDefaultField.get(null);
        }

        //　获取mIntance
        Field mInstanceField = loader.loadClass("android.util.Singleton").getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object mInstance = mInstanceField.get(gDefault);

        // 替换mIntance
        Object proxy = Proxy.newProxyInstance(mInstance.getClass().getClassLoader(),new Class[]{loader.loadClass("android.app.IActivityManager")},new IActivityManagerHandler(mInstance));
        mInstanceField.set(gDefault, proxy);

        //　获取ActivityThread实例
        Class activityThreadClass = loader.loadClass("android.app.ActivityThread");
        Field threadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        threadField.setAccessible(true);
        Object sCurrentActivityThread = threadField.get(null);

        //　获取mH变量
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        Object mH = mHField.get(sCurrentActivityThread);

        //　设置mCallback变量
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        Handler.Callback callback = msg -> {
            if (msg.what == 100) {
                try {
                    Field intentField = msg.obj.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent intent = (Intent) intentField.get(msg.obj);
                    Intent raw = intent.getParcelableExtra("RawIntent");
                    intent.setComponent(raw.getComponent());
                } catch (Exception e) {
                    printLog(e.getMessage());
                }
            }
            return false;
        };
        mCallbackField.set(mH, callback);

        Class<?> clazz_Ath = Class.forName("android.app.ActivityThread");
        Class<?> clazz_LApk = Class.forName("android.app.LoadedApk");

        Object currentActivityThread = clazz_Ath.getMethod("currentActivityThread").invoke(null);
        Field field1 = clazz_Ath.getDeclaredField("mPackages");
        field1.setAccessible(true);
        Map mPackages = (Map) field1.get(currentActivityThread);

        WeakReference ref = (WeakReference) mPackages.get(mApplicationContext.getPackageName());
        Field field2 = clazz_LApk.getDeclaredField("mClassLoader");
        field2.setAccessible(true);
        field2.set(Objects.requireNonNull(ref).get(), dcLoader);
    }

    static class IActivityManagerHandler implements InvocationHandler {
        private Object mOrigin;

        IActivityManagerHandler(Object origin) {
            mOrigin = origin;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".equals(method.getName())) {
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }
                Intent raw = (Intent) args[index];

                try {
                    if (!raw.getComponent().getClassName().equals("net.pois0nbread.unyoungmodel4bili.hook.MyLoginActivity")) {
                        printLog("not change Intent");
                        return method.invoke(mOrigin, args);
                    }
                } catch (Exception e) {
                    printLog("not change Intent" + e.getMessage());
                    return method.invoke(mOrigin, args);
                }

                Intent intent = new Intent();
                intent.setClassName(raw.getComponent().getPackageName(), "com.bsgamesdk.android.activity.ExitActivity");
                intent.putExtra("RawIntent", raw);
                args[index] = intent;
            }
            return method.invoke(mOrigin, args);
        }
    }
}
