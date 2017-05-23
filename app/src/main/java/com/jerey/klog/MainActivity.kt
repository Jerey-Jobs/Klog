package com.jerey.klog

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jerey.loglib.log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var s = "nihao"
        s.log<String>()
         .toUpperCase()
         .log<String>()
         .toLowerCase()
         .log<String>()

    }
}
