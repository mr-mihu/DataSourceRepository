package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IRankListModel
import org.jsoup.select.Elements

class CustomRankListModel : IRankListModel {
    private var bgTimes = 0
    var rankList: MutableList<Any> = ArrayList()

    override suspend fun getRankListData(partUrl: String): Pair<MutableList<Any>, PageNumberBean?> {
        rankList.clear()
        if (partUrl == "/" || partUrl == "") getWeekRankData()
        else getAllRankData(partUrl)
        return Pair(rankList, null)
    }

    private suspend fun getAllRankData(partUrl: String) {
        val document = JsoupUtil.getDocument(CustomConst.MAIN_URL + CustomConst.ANIME_RANK)
        val areaChildren: Elements = document.select("[class=area]")[0].children()
        for (i in areaChildren.indices) {
            when (areaChildren[i].className()) {
                "topli" -> {
                    rankList.addAll(ParseHtmlUtil.parseTopli2(areaChildren[i]))
                }
            }
        }
    }

    private suspend fun getWeekRankData() {
        bgTimes = 0
        val url = CustomConst.MAIN_URL
        val document = JsoupUtil.getDocument(url)
        val areaChildren: Elements = document.select("[class=area]")[0].children()
        for (i in areaChildren.indices) {
            when (areaChildren[i].className()) {
                "side r" -> {
                    val sideRChildren = areaChildren[i].children()
                    for (j in sideRChildren.indices) {
                        when (sideRChildren[j].className()) {
                            "bg" -> {
                                if (bgTimes++ == 0) continue

                                val bgChildren = sideRChildren[j].children()
                                for (k in bgChildren.indices) {
                                    when (bgChildren[k].className()) {
                                        "pics" -> {
                                            rankList.addAll(
                                                ParseHtmlUtil.parsePics2(bgChildren[k], url)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
