package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.ISearchModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.Util.toEncodedUrl
import org.jsoup.select.Elements

class CustomSearchModel : ISearchModel {
    override suspend fun getSearchData(
        keyWord: String,
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        var pageNumberBean: PageNumberBean? = null
        val searchResultList: ArrayList<Any> = ArrayList()

        val url = if (partUrl.isBlank())
            "${Api.MAIN_URL}${CustomConst().actionUrl.ANIME_SEARCH()}-------------.html?wd=${keyWord.toEncodedUrl()}"
        else Api.MAIN_URL + partUrl

        val element = JsoupUtil.getDocument(url)

        //页面数据列表
        var listElements = element.select("body>div.area")

        for (i in listElements.indices) {
            var className = listElements[i].className()
            if ("area" != className) {
                continue
            }

            //视频数据列表
            searchResultList.addAll(CustomParseHtmlUtil.parseLVideo(listElements[i], url))
            //TODO 未完成 图片数据列表
//            searchResultList.addAll(ParseHtmlUtil.parseLPic(listElements[i], url))
        }

        //分页数据
        var pageElement: Elements = element.select("div.pagination")
        pageNumberBean = CustomParseHtmlUtil.parseNextPages(pageElement)

        return Pair(searchResultList, pageNumberBean)
    }
}