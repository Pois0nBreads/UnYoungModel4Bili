package com.android.data.sdk;

import android.app.Activity;

import com.android.data.sdk.domain.interfaces.ILifeCycle;
import com.android.data.sdk.domain.model.DataParamsModel;
import com.android.data.sdk.domain.model.DataUpModel;

public class Main implements ILifeCycle {
    @Override
    public void appDestroy(Activity activity) {}

    @Override
    public void appOffline(Activity activity) {}

    @Override
    public void appOnline(Activity activity) {}

    @Override
    public void stop(Activity activity) {}

    public Main(PreDefined preDefined) {}

    public void dCInit(final Activity activity, final DataParamsModel dataParamsModel, final String str) {}

    protected void readChannelId(Activity activity, DataUpModel dataUpModel) {}


}
