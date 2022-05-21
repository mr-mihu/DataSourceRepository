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
//            uriString.startsWith(CustomConst.ANIME_SEARCH) -> {
//                uriString.replace(CustomConst.ANIME_SEARCH, "").let {
//                    val keyword = it.replaceFirst(Regex("/.*"), "")
//                    val pageNumber = it.replaceFirst(Regex("($keyword/)|($keyword)"), "")
//                    SearchActivityProcessor.route.buildRouteUri {
//                        appendQueryParameter("keyword", keyword)
//                        appendQueryParameter("pageNumber", pageNumber)
//                    }.route(context)
//                }
//                return true
//            }
//            uriString.startsWith(CustomConst.ANIME_RANK) -> {
//                RankActivityProcessor.route.route(context)
//                return true
//            }
            uriString.startsWith(CustomConst.ANIME_PLAY) -> {
                PlayActivityProcessor.route.buildRouteUri {
                    appendQueryParameter("partUrl", uriString)
                }.route(context)
                return true
            }
        }
        return false
    }
}
