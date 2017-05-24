package com.jerey.klog

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import com.jerey.loglib.KLog
import com.jerey.loglib.log

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var str = "nihao"
        str.log()
           .toUpperCase()
           .log("upper")
           .toLowerCase()
           .log("lower")

        KLog.getSettings()
            .setBorderEnable(true)

        KLog.a("aaaaaaa")
        KLog.a(contents = "bbbbb")

        var list = arrayListOf<String>("aaa","bb","cccc", "ddddd")
        list.log("init")
                .map { it -> it.toUpperCase() }
                .log("after map")
                .filter { it -> it.length > 2 }
                .log("after filter")


    }
}
