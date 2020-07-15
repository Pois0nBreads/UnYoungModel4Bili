package net.pois0nbread.unyoungmodel4bili.util;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/07/14
 *     desc   : MyClassLoader
 *     version: 2.2.0
 * </pre>
 */

public class MyClassLoader extends BaseDexClassLoader {

    private ClassLoader mBaseClassLoader, mPathClassLoader;
    private ArrayList<String> mArrayList = new ArrayList<>();

    public MyClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, null, null, null);
        this.mBaseClassLoader = parent;
        this.mPathClassLoader = new PathClassLoader(dexPath, getSystemClassLoader());
    }

    public void addClassName(String... className){
        for (String name : className)
            if (!mArrayList.contains(name))
                mArrayList.add(name);
    }

    public void removeClassName(String... className) {
        for (String name : className)
            mArrayList.remove(name);
    }

    public void clearClassName() {
        mArrayList.clear();
    }

    public ArrayList getClassNameList(){
        return (ArrayList) mArrayList.clone();
    }

    public void setClassNameList(ArrayList<String> classNameList){
        this.mArrayList = classNameList;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (mArrayList.contains(name))
            return mPathClassLoader.loadClass(name);
        try {
            return mBaseClassLoader.loadClass(name);
        } catch (Exception e) {
            try {
                return MyClassLoader.class.getClassLoader().loadClass(name);
            } catch (Exception ee) {
                ee.printStackTrace();
                throw new ClassNotFoundException(name + " Not Found");
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return loadClass(name);
    }

    @SuppressLint("PrivateApi")
    public static void replaceClassLoader1(ClassLoader dexClassLoader, ClassLoader parent){
        try {
            Class clzActivityThead = parent.loadClass("android.app.ActivityThread");
            Class clzLoadedApk = parent.loadClass("android.app.LoadedApk");
            Class clzAppBindData = parent.loadClass("android.app.ActivityThread$AppBindData");

            Method currentActivityThread = clzActivityThead.getMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);

            Field clzActivity_mBoundApplication = clzActivityThead.getDeclaredField("mBoundApplication");
            Field clzAppBind_info = clzAppBindData.getDeclaredField("info");
            Field clzLoadedApk_mClassLoader = clzLoadedApk.getDeclaredField("mClassLoader");
            clzActivity_mBoundApplication.setAccessible(true);
            clzAppBind_info.setAccessible(true);
            clzLoadedApk_mClassLoader.setAccessible(true);

            Object objActivityThread = currentActivityThread.invoke(null);
            Object data = clzActivity_mBoundApplication.get(objActivityThread);
            Object info = clzAppBind_info.get(data);

            clzLoadedApk_mClassLoader.set(info,dexClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
