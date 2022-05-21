package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IEverydayAnimeModel
import org.jsoup.select.Elements

class CustomEverydayAnimeModel : IEverydayAnimeModel {
    override suspend fun getEverydayAnimeData(): Triple<ArrayList<TabBean>, ArrayList<List<Any>>, String> {
        val tabList = ArrayList<TabBean>()
        var header = ""
        val everydayAnimeList: ArrayList<List<Any>> = ArrayList()
        val document = JsoupUtil.getDocument(CustomConst.MAIN_URL)
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
                                        "dtit" -> {
                                            header = ParseHtmlUtil.parseDtit(bgChildren[k])
                                        }
                                        "tag" -> {
                                            val tagChildren = bgChildren[k].children()
                                            for (l in tagChildren.indices) {
                                                tabList.add(
                                                    TabBean(
                                                        "",
                                                        "",
                                                        tagChildren[l].text()
                                                    )
                                                )
                                            }
                                        }
                                        "tlist" -> {
                                            everydayAnimeList.addAll(
                                                ParseHtmlUtil.parseTlist2(bgChildren[k])
                                            )
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
        return Triple(tabList, everydayAnimeList, header)
    }
}