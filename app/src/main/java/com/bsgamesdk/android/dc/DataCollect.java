package com.bsgamesdk.android.dc;

import android.app.Activity;

import com.android.data.sdk.domain.interfaces.ILifeCycle;
import com.android.data.sdk.domain.model.DataParamsModel;

public class DataCollect implements ILifeCycle {

    private static DataCollect mDataCollect = new DataCollect();

    public static DataCollect getInstance() {return mDataCollect;}

    public void dCInit(Activity activity, DataParamsModel dataParamsModel2, String str, ExitCallbackListener exitCallbackListener) {}

    @Override
    public void appDestroy(Activity activity) {}

    @Override
    public void appOffline(Activity activity) {}

    @Override
    public void appOnline(Activity activity) {}

    @Override
    public void stop(Activity activity) {}
}
