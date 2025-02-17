package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.ISearchModel
import com.skyd.imomoe.util.Util
import org.jsoup.select.Elements

class CustomSearchModel : ISearchModel {
    override suspend fun getSearchData(
        keyWord: String,
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        var pageNumberBean: PageNumberBean? = null
        val searchResultList: ArrayList<Any> = ArrayList()
        val url = if (partUrl.isBlank())
            "${CustomConst.MAIN_URL}${CustomConst.ANIME_SEARCH}?kw=${Util.getEncodedUrl(keyWord)}"
        else CustomConst.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        val fireL = document.getElementsByClass("area").select("[class=fire l]")
        val lpic: Elements = fireL.select("[class=lpic]")
        searchResultList.addAll(CustomParseHtmlUtil.parseLpic(lpic[0], url))
        val pages = fireL[0].select("[class=pages]")
        if (pages.size > 0) pageNumberBean = CustomParseHtmlUtil.parseNextPages(pages[0])
        return Pair(searchResultList, pageNumberBean)
    }
}