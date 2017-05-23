package com.jerey.loglib

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import java.util.*
import java.util.concurrent.ExecutorService
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8�� │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐ ┌┐ ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘ └┘ └┘
 * ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬���──┬───┬───┬───┬───────┐ ┌───┬───┬───┐ ┌───┬───┬───┬───┐
 * │~ `│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp │ │Ins│Hom│PUp│ │N L│ / │ * │ - │
 * ├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤ ├───┼───┼───┤ ├───┼───┼───┼───┤
 * │ Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ │ │Del│End│PDn│ │ 7 │ 8 │ 9 │ │
 * ├─────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤ └───┴───┴───┘ ├───┼───┼───┤ + │
 * │ Caps │ A │ S ��� D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter │ │ 4 │ 5 │ 6 │ │
 * ├──────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤ ┌───┐ ├───┼───┼───┼───┤
 * │ Shift │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│ Shift │ │ ↑ │ │ 1 │ 2 │ 3 │ │
 * ├─────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤ ┌───┼───┼───┐ ├───┴───┼───┤ E││
 * │ Ctrl│ │Alt │ Space │ Alt│ │ │Ctrl│ │ ← │ ↓ │ → │ │ 0 │ . │←─┘│
 * └─────┴────┴────┴───────────────────────┴────┴────┴────┴────┘ └───┴───┴───┘ └───────┴───┴───┘
 * Created by xiamin on 3/29/17.
 */
/**
 * <pre>
 * author: xiamin
 * blog : http://jerey.cn
 * desc : 一个可控制边框美化,可控制打印等级,可控制Log info, 自动识别类名为Tag, 同时支持自定义Tag的日志工具库
 * </pre>
 */


/**
 * Created by xiamin on 5/23/17.
 */

fun <T> Any.log(): T {
    KLog.i(contents = toString())
    return this as T
}


class KLog private constructor() {
    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    /**
     * LogTools设置类
     */
    class Settings {
        /**
         * 设置Log是否开启
         *
         * @param enable
         * @return
         */
        fun setLogEnable(enable: Boolean): Settings {
            KLog.mLogEnable = enable
            return this
        }

        /**
         * 设置打印等级,只有高于该打印等级的log会被打印<br>
         * 打印等级从低到高分别为: Log.VERBOSE < Log.DEBUG < Log.INFO < Log.WARN < Log.ERROR < Log.ASSERT
         *
         * @param logLevel
         */
        fun setLogLevel(logLevel: Int): Settings {
            KLog.mLogFilter = logLevel
            return this
        }

        /**
         * 设置边框是否开启
         *
         * @param enable
         * @return
         */
        fun setBorderEnable(enable: Boolean): Settings {
            KLog.mLogBorderEnable = enable
            return this
        }

        /**
         * 设置Log 行号,方法,class详情信息是否打印的开关
         *
         * @param enable
         * @return
         */
        fun setInfoEnable(enable: Boolean): Settings {
            KLog.mLogInfoEnable = enable
            return this
        }

        /**
         * 获取打印等级
         *
         * @return
         */
        val logLevel: Int
            get() {
                return KLog.mLogFilter
            }

        /**
         * 设置log保存目录,若不设置,默认不保存
         *
         * @param dir
         * @return
         */
        fun setLogSaveDir(dir: String): Settings {
            KLog.mLogDir = dir
            return this
        }
    }

    companion object {
        private val JSON = -1
        private val XML = -2
        private val MAX_LEN = 4000
        private val TOP_BORDER = "╔═════════════════════════════════════════════════════════════════════════════════════"
        private val LEFT_BORDER = "║ "
        private val BOTTOM_BORDER = "╚═════════════════════════════════════════════════════════════════════════════════════"
        //解决windows和linux换行不一致的问题 功能和"\n"是一致的,但是此种写法屏蔽了 Windows和Linux的区别 更保险.
        private val LINE_SEPARATOR = System.getProperty("line.separator")
        private val NULL_TIPS = "Log with a null object;"
        private val NULL = "null"
        private val ARGS = "args"
        private var mLogDir: String = "" // log存储目录
        private var mLogEnable = true // log总开关
        private val mGlobalLogTag = "" // log标签
        private val mTagIsSpace = true // log标签是否为空白
        private val mLog2FileEnable = false// log是否写入文件
        private var mLogBorderEnable = true // log边框
        private var mLogInfoEnable = true // log详情开关
        private var mLogFilter = Log.VERBOSE // log过滤器

        fun i(tag: String = mGlobalLogTag, vararg contents: Any) {
            log(Log.INFO, tag, contents)
        }

        fun w(contents: Any) {
            log(Log.WARN, mGlobalLogTag, contents)
        }

        fun w(tag: String, vararg contents: Any) {
            log(Log.WARN, tag, contents)
        }

        fun e(contents: Any) {
            log(Log.ERROR, mGlobalLogTag, contents)
        }

        fun e(tag: String, vararg contents: Any) {
            log(Log.ERROR, tag, contents)
        }

        fun a(vararg contents: Any) {
            log(Log.ASSERT, mGlobalLogTag, contents)
        }

        fun a(tag: String, vararg contents: Any) {
            log(Log.ASSERT, tag, contents)
        }

        fun d(contents: Any) {
            log(Log.DEBUG, mGlobalLogTag, contents)
        }

        fun d(tag: String, vararg contents: Any) {
            log(Log.DEBUG, tag, contents)
        }

        fun json(contents: String) {
            log(JSON, mGlobalLogTag, contents)
        }

        fun json(tag: String, vararg contents: Any) {
            log(JSON, tag, *contents)
        }

        fun xml(contents: Any) {
            log(XML, mGlobalLogTag, contents)
        }

        fun xml(tag: String, vararg contents: Any) {
            log(XML, tag, *contents)
        }

        /**
         * @param type
         * @param tag
         * @param objects
         */
        private fun log(type: Int, tag: String, vararg objects: Any) {
            Log.i("xiamin", "log ->type: $type tag:$tag content:$objects")
            //全局未开,直接返回
            if (!mLogEnable) {
                return
            }
            val processContents = processObj(type, tag, *objects)
            var tagret = processContents[0]
            val msg = processContents[1]
            when (type) {
                Log.INFO, Log.ASSERT, Log.DEBUG, Log.ERROR, Log.WARN -> if (mLogFilter <= type) {
                    logOutout(type, tagret, msg)
                }
                JSON -> logOutout(Log.DEBUG, tagret, msg)
                XML -> logOutout(Log.DEBUG, tagret, msg)
            }
        }

        private fun processObj(type: Int, tags: String, vararg contents: Any): Array<String> {
            var tag = tags
            val targetElement = Thread.currentThread().stackTrace[5]
            var className = targetElement.className
            val classNameInfo = className.split(("\\.").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (classNameInfo.size > 0) {
                className = classNameInfo[classNameInfo.size - 1]
            }
            if (className.contains("$")) {
                className = className.split(("\\$").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
            }
            Log.i("xiamin",className + " " + tag);
            if (!mTagIsSpace) {// 如果全局tag不为空，那就用全局tag
                tag = mGlobalLogTag
            } else {// 全局tag为空时，如果传入的tag为空那就显示类名，否则显示tag
                tag = if (TextUtils.isEmpty(tag) || isSpace(tag)) className else tag
            }
            Log.i("xiamin",className + " " + tag);
            val head = Formatter()
                    .format("Thread: %s, Method: %s (%s.java Line:%d)" + LINE_SEPARATOR,
                            Thread.currentThread().name,
                            targetElement.methodName,
                            className,
                            targetElement.lineNumber)
                    .toString()
            var msg = NULL_TIPS
            if (contents != null) {
                //正常使用情况下都是只有一个参数
                if (contents.size == 1) {
                    val `object` = contents[0]
                    msg = if (`object` == null) NULL else `object`.toString()
                    if (type == JSON) {
                        msg = formatJson(msg)
                    } else if (type == XML) {
                        msg = formatXml(msg)
                    }
                } else {
                    val sb = StringBuilder()
                    var i = 0
                    val len = contents.size
                    while (i < len) {
                        val content = contents[i]
                        sb.append(ARGS)
                                .append("[")
                                .append(i)
                                .append("]")
                                .append(" = ")
                                .append(if (content == null) NULL else content.toString())
                                .append(LINE_SEPARATOR)
                        i += 1
                    }
                    msg = sb.toString()
                }
            }
            if (mLogBorderEnable) {
                val sb = StringBuilder()
                val lines = msg.split((LINE_SEPARATOR).toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                sb.append(TOP_BORDER).append(LINE_SEPARATOR)
                sb.append(LEFT_BORDER).append(head)
                for (line in lines) {
                    sb.append(LEFT_BORDER).append(line).append(LINE_SEPARATOR)
                }
                sb.append(BOTTOM_BORDER).append(LINE_SEPARATOR)
                msg = sb.toString()
                return arrayOf<String>(tag, msg)
            }
            if (mLogInfoEnable) {
                val sb = StringBuilder()
                val lines = msg.split((LINE_SEPARATOR).toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                for (line in lines) {
                    sb.append(line).append(LINE_SEPARATOR)
                }
                msg = sb.toString()
                return arrayOf<String>(tag, head + msg)
            }
            return arrayOf<String>(tag, msg)
        }

        private fun formatJson(json: String): String {
            var ret: String = json
            try {
                if (ret.startsWith("{")) {
                    ret = JSONObject(ret).toString(4)
                } else if (ret.startsWith("[")) {
                    ret = JSONArray(ret).toString(4)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return ret
        }

        private fun formatXml(xml: String): String {
            try {
                val xmlInput = StreamSource(StringReader(xml))
                val xmlOutput = StreamResult(StringWriter())
                val transformer = TransformerFactory.newInstance().newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
                transformer.transform(xmlInput, xmlOutput)
                return xmlOutput.writer.toString().replaceFirst((">").toRegex(), ">" + LINE_SEPARATOR)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return xml
        }

        /**
         * 输出log
         *
         * @param type
         * @param tag
         * @param msg
         */
        private fun logOutout(type: Int, tag: String, msg: String) {
            val len = msg.length
            val countOfSub = len / MAX_LEN
            if (countOfSub > 0) {
                var index = 0
                var sub: String
                for (i in 0..countOfSub - 1) {
                    sub = msg.substring(index, index + MAX_LEN)
                    printSubLog(type, tag, sub)
                    index += MAX_LEN
                }
                printSubLog(type, tag, msg.substring(index, len))
            } else {
                printSubLog(type, tag, msg)
            }
        }

        private fun printSubLog(type: Int, tag: String, msg: String) {
            when (type) {
                Log.VERBOSE -> Log.v(tag, msg)
                Log.DEBUG -> Log.d(tag, msg)
                Log.INFO -> Log.i(tag, msg)
                Log.WARN -> Log.w(tag, msg)
                Log.ERROR -> Log.e(tag, msg)
                Log.ASSERT -> Log.wtf(tag, msg)
            }
        }

        private fun isSpace(s: String): Boolean {
            return (0..s?.length - 1).any { Character.isWhitespace(s[it]) }
        }

        /**
         * 设置入口
         * <code>
         * LogTools.getSettings()
         * .setLogLevel(Log.WARN)
         * .setBorderEnable(true)
         * .setLogEnable(true);
         * </code>
         *
         * @return
         */
        val settings: Settings
            get() {
                return Settings()
            }

//        fun monitorLifeCycle(application: Application) {
//            application.registerActivityLifecycleCallbacks(object : )
//        }
//
//        val lifeCallback = Application.ActivityLifecycleCallbacks() {
//            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
//                LogTools.d(activity.getComponentName().getClassName(), "onCreate")
//            }
//
//            override fun onActivityStarted(activity: Activity) {
//                LogTools.d(activity.getComponentName().getClassName(), "onStart")
//            }
//
//            override fun onActivityResumed(activity: Activity) {
//                LogTools.d(activity.getComponentName().getClassName(), "onResume")
//            }
//
//            override fun onActivityPaused(activity: Activity) {
//                LogTools.d(activity.getComponentName().getClassName(), "onPause")
//            }
//
//            override fun onActivityStopped(activity: Activity) {
//                LogTools.d(activity.getComponentName().getClassName(), "onStop")
//            }
//
//            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//                LogTools.d(activity.getComponentName().getClassName(), "onSaveInstance")
//            }
//
//            override fun onActivityDestroyed(activity: Activity) {
//                LogTools.d(activity.getComponentName().getClassName(), "onDestroy")
//            }
//        }
    }
}