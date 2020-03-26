package com.sk.qqfilemove

import android.content.Context
import java.lang.reflect.Field


class ScreenUtils {
    companion object{
        /**
         * 获取状态栏/通知栏的高度
         *
         * @return
         */
        open fun getStatusBarHeight(context: Context): Int {
            var c: Class<*>? = null
            var obj: Any? = null
            var field: Field? = null
            var x = 0
            var sbar = 0
            try {
                c = Class.forName("com.android.internal.R\$dimen")
                obj = c.newInstance()
                field = c.getField("status_bar_height")
                x = field.get(obj).toString().toInt()
                sbar = context.getResources().getDimensionPixelSize(x)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            return sbar
        }
    }
}