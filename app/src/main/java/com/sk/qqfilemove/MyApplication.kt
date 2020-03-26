package com.sk.qqfilemove

import android.R
import android.app.Application
import android.content.Context
import android.os.Environment
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.*
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader


class MyApplication : Application() {
    companion object{
        init {
            //设置全局的Header构建器
            //设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator(object : DefaultRefreshHeaderCreator {
                override fun createRefreshHeader(
                    context: Context,
                    layout: RefreshLayout
                ): RefreshHeader {
                    layout.setPrimaryColorsId(
                        R.color.white,
                        R.color.black
                    ) //全局设置主题颜色
                    return ClassicsHeader(context) //.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
                }
            })
            //设置全局的Footer构建器
            //设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator(object : DefaultRefreshFooterCreator {
                override fun createRefreshFooter(
                    context: Context,
                    layout: RefreshLayout
                ): RefreshFooter {
                    return ClassicsFooter(context).setDrawableSize(20f)
                }
            })
        }
    }
}