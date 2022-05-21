package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IEverydayAnimeWidgetModel
import com.skyd.imomoe.model.impls.custom.ParseHtmlUtil.parseTlist
import org.jsoup.select.Elements

class CustomEverydayAnimeWidgetModel : IEverydayAnimeWidgetModel {
    override fun getEverydayAnimeData(): ArrayList<List<AnimeCover10Bean>> {
        val list: ArrayList<List<AnimeCover10Bean>> = ArrayList()
        try {
            val document = JsoupUtil.getDocumentSynchronously(CustomConst.MAIN_URL)
            val areaChildren: Elements = document.select("[class=area]")[0].children()
            for (i in areaChildren.indices) {
                when (areaChildren[i].className()) {
                    "side r" -> {
                        val sideRChildren = areaChildren[i].children()
                        out@ for (j in sideRChildren.indices) {
                            when (sideRChildren[j].className()) {
                                "bg" -> {
                                    val bgChildren = sideRChildren[j].children()
                                    for (k in bgChildren.indices) {
                                        when (bgChildren[k].className()) {
                                            "tlist" -> {
                                                list.addAll(parseTlist(bgChildren[k]))
                                            }
                                        }
                                    }
                                    break@out
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}