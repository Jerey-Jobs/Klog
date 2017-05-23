package com.jerey.loglib

import android.util.Log

/**
 * Created by xiamin on 5/23/17.
 */


fun <T> Any.log():T {
    Log.i("xiamin",toString())
    return this as T
}