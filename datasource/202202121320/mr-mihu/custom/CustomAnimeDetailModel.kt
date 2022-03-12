package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.ImageBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.IAnimeDetailModel
import com.skyd.imomoe.model.util.JsoupUtil

class CustomAnimeDetailModel : IAnimeDetailModel {
    override suspend fun getAnimeDetailData(
        partUrl: String
    ): Triple<ImageBean, String, ArrayList<Any>> {
        val animeDetailList: ArrayList<Any> = ArrayList()
        val url = Api.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        //获取节点
        var element = document.body()

        //封面图片
        var img = element.select("div.detail-poster a img").attr("src")
        val cover = ImageBean("", img, url)

        //标题
        val title = element.select("div.breadcrumbs span").text()

        /* 每行模块数据 */
        //封面和类型相关信息
        CustomParseHtmlUtil.addInfoBean(url,element,animeDetailList)

        //播放器和集列表
        CustomParseHtmlUtil.getPlayerRowsData(url,element,animeDetailList)

        return Triple(cover, title, animeDetailList)
    }
}