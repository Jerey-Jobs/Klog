package com.jerey.klog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jerey.loglib.Klog;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Klog.Companion.i("test");
        Klog.Companion
                .getSettings()
                .setBorderEnable(false);

    }
}
