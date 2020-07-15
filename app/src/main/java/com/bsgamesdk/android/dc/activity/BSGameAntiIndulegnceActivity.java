package com.bsgamesdk.android.dc.activity;

import android.app.Activity;
import android.os.Bundle;

public class BSGameAntiIndulegnceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getBooleanExtra("isFromXposed", false)) finish();
    }
}
