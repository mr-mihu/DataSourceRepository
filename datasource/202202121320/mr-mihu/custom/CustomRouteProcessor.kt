package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.skyd.imomoe.config.Const.ActionUrl.Companion.ANIME_CLASSIFY
import com.skyd.imomoe.model.interfaces.IRouteProcessor
import com.skyd.imomoe.util.Util.getSubString
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.view.activity.AnimeDetailActivity
import com.skyd.imomoe.view.activity.ClassifyActivity
import com.skyd.imomoe.view.activity.PlayActivity
import com.skyd.imomoe.view.activity.SearchActivity
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder

class CustomRouteProcessor : IRouteProcessor {

    override fun process(context: Context, actionUrl: String): Boolean {
        val decodeUrl = URLDecoder.decode(actionUrl, "UTF-8")
        val const = CustomConst()
        var solved = true
        when {
            decodeUrl.startsWith(const.actionUrl.ANIME_DETAIL()) -> {     //番剧封面点击进入
                context.startActivity(
                    Intent(context, AnimeDetailActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("partUrl", actionUrl)
                )
            }
            decodeUrl.startsWith(const.actionUrl.ANIME_PLAY()) -> {     //番剧每一集点击进入
                val playCode = actionUrl.getSubString("\\/vodplay\\/", "\\.")[0].split("-")
                if (playCode.size >= 3) {
                    var detailPartUrl = actionUrl.substringAfter(const.actionUrl.ANIME_DETAIL(), "")
                    detailPartUrl = const.actionUrl.ANIME_DETAIL() + detailPartUrl
                    context.startActivity(
                        Intent(context, PlayActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(
                                "partUrl",
                                actionUrl.substringBefore(const.actionUrl.ANIME_DETAIL())
                            )
                            .putExtra("detailPartUrl", detailPartUrl)
                    )
                } else {
                    "播放集数解析错误！".showToast()
                }
            }
        }
        return solved
    }

    override fun process(activity: Activity, actionUrl: String): Boolean {
        val decodeUrl = URLDecoder.decode(actionUrl, "UTF-8")
        val const = CustomConst()
        var solved = true
        when {
            //如进入分类页面
            decodeUrl.startsWith(ANIME_CLASSIFY) -> {
                val paramList = actionUrl.replace(ANIME_CLASSIFY, "").split("/")
                if (paramList.size == 4) {      //例如  /list/?label=恋爱/恋爱  分割后是3个参数：""，list，?label=恋爱，恋爱
                    activity.startActivity(
                        Intent(activity, ClassifyActivity::class.java)
                            .putExtra("partUrl", "/${paramList[1]}/${paramList[2]}")
                            .putExtra("classifyTabTitle", "")
                            .putExtra("classifyTitle", paramList[3])
                    )
                } else "跳转协议格式错误".showToast()
            }
            // 排行榜
            decodeUrl.startsWith(const.actionUrl.ANIME_RANK()) -> {
                "这里什么都没有哦！".showToast(Toast.LENGTH_LONG)
                //activity.startActivity(Intent(activity, RankActivity::class.java))
            }
            // 进入搜索页面
            decodeUrl.startsWith(const.actionUrl.ANIME_SEARCH()) -> {
                val paramMap: HashMap<String, String> = HashMap()
                try {
                    URL(const.MAIN_URL() + actionUrl).query?.let { query ->
                        query.split("&").forEach { kv ->
                            kv.split("=").let { v ->
                                paramMap[v[0]] = v[1]
                            }
                        }
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
                decodeUrl.replace(const.actionUrl.ANIME_SEARCH(), "").let {
                    val keyWord: String = paramMap["kw"] ?: ""
                    activity.startActivity(
                        Intent(activity, SearchActivity::class.java)
                            .putExtra("keyWord", keyWord)
                            .putExtra("pageNumber", actionUrl)
                    )
                }
            }
            //番剧封面点击进入
            decodeUrl.startsWith(const.actionUrl.ANIME_DETAIL()) -> {
                activity.startActivity(
                    Intent(activity, AnimeDetailActivity::class.java)
                        .putExtra("partUrl", actionUrl)
                )
            }
            //番剧每一集点击进入
            decodeUrl.startsWith(const.actionUrl.ANIME_PLAY()) -> {
                val playCode = actionUrl.getSubString("\\/vodplay\\/", "\\.")[0].split("-")
                if (playCode.size >= 3) {
                    var detailPartUrl = actionUrl.substringAfter(const.actionUrl.ANIME_PLAY(), "")
                    detailPartUrl = const.actionUrl.ANIME_DETAIL() + detailPartUrl
                    activity.startActivity(
                        Intent(activity, PlayActivity::class.java)
                            .putExtra(
                                "partUrl",
                                actionUrl.substringBefore(const.actionUrl.ANIME_DETAIL())
                            )
                            .putExtra("detailPartUrl", detailPartUrl)
                    )
                } else {
                    "播放线路解析错误！".showToast()
                }
            }
            else -> solved = false
        }
        return solved
    }

}