package com.jerey.klog

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import com.jerey.loglib.Klog
import com.jerey.loglib.log

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * Klog 使用demo
         */
        var str = "nihao"
        str.log()                   //直接输出该对象toString
                .toUpperCase()
                .log("upper")             //输出带提示的处理结果
                .toLowerCase()            //继续处理
                .log("lower")

        //修改Klog设置, 开启边框打印
        Klog.getSettings()
                .setBorderEnable(true)

        Klog.a("aaaaaaa")            //普通log输出方式1
        Klog.a(contents = "bbbbb")   //普通log输出方式2
        Klog.i("jerey", "aaaaaaa")    //带tag输出

        var list = arrayListOf<String>("aaa", "bb", "cccc", "ddddd")
        list.log("init")
                .map { it -> it.toUpperCase() }
                .log("after map")
                .filter { it -> it.length > 2 }
                .log("after filter")
    }
}
