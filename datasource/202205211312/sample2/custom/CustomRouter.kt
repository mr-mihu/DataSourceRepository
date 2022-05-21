package com.skyd.imomoe.model.impls.custom

import android.content.Context
import android.net.Uri
import com.skyd.imomoe.model.interfaces.IRouter
import com.skyd.imomoe.route.Router.buildRouteUri
import com.skyd.imomoe.route.Router.route
import com.skyd.imomoe.route.processor.DetailActivityProcessor
import com.skyd.imomoe.route.processor.PlayActivityProcessor
import com.skyd.imomoe.route.processor.RankActivityProcessor
import com.skyd.imomoe.route.processor.SearchActivityProcessor
import com.skyd.imomoe.util.Util.getSubString
import com.skyd.imomoe.util.showToast
import java.net.MalformedURLException
import java.net.URL


class CustomRouter : IRouter {
    override fun route(uri: Uri, context: Context?): Boolean {
        val uriString = uri.toString()
        when {
            uriString.startsWith(CustomConst.ANIME_DETAIL) -> {
                DetailActivityProcessor.route.buildRouteUri {
                    appendQueryParameter("partUrl", uriString)
                }.route(context)
                return true
            }
            uriString.startsWith(CustomConst.ANIME_SEARCH) -> {
                val paramMap: HashMap<String, String> = HashMap()
                try {
                    URL(CustomConst.MAIN_URL + uriString).query?.let { query ->
                        query.split("&").forEach { kv ->
                            kv.split("=").let { v ->
                                paramMap[v[0]] = v[1]
                            }
                        }
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
                uriString.replace(CustomConst.ANIME_SEARCH, "").let {
                    val keyword: String = paramMap["kw"] ?: ""
                    SearchActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("keyword", keyword)
                        appendQueryParameter("pageNumber", uriString)
                    }.route(context)
                }
                return true
            }
            uriString.startsWith(CustomConst.ANIME_RANK) -> {
                RankActivityProcessor.route.route(context)
                return true
            }
            uriString.startsWith(CustomConst.ANIME_PLAY) -> {
                val playCode = uriString.getSubString("\\/vp\\/", "\\.")[0].split("-")
                if (playCode.size >= 2) {
                    val detailPartUrl = CustomUtil().getDetailLinkByEpisodeLink(uriString)
                    PlayActivityProcessor.route.buildRouteUri {
                        appendQueryParameter("partUrl", uriString)
                        appendQueryParameter("detailPartUrl", detailPartUrl)
                    }.route(context)
                } else {
                    "播放集数解析错误！".showToast()
                }
                return true
            }
            uriString.startsWith(CustomConst.ANIME_LINK) -> {
                "暂不支持带有referer的外部浏览器跳转".showToast()
                return true
            }
        }
        return false
    }
}
