package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.Header1Bean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.IAnimeShowModel
import com.skyd.imomoe.model.util.JsoupUtil
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class CustomAnimeShowModel : IAnimeShowModel {
    override suspend fun getAnimeShowData(
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        val url = Api.MAIN_URL + partUrl
        var pageNumberBean: PageNumberBean? = null
        val animeShowList: ArrayList<Any> = ArrayList()

        if("/" == partUrl){
            //首页为错误页，需要从新获取最新地址
            updateHomeDate(animeShowList)
        }

        try {
            val document = JsoupUtil.getDocument(url)
            //获取节点
            var element = document.body()

            //分页数据
            var pageElement: Elements = element.select("div.pagination")
            //有分页
            pageNumberBean = CustomParseHtmlUtil.parseNextPages(pageElement)

            //非首页标签
            setNotHomeDate(partUrl, element, animeShowList)
        }catch (e:Exception){
            //请求失败，则从新获取最新地址
            updateHomeDate(animeShowList)
        }

        return Pair(animeShowList, pageNumberBean)
    }

    /**
     * 非首页tab标签
     */
    fun setNotHomeDate(
        partUrl: String,
        element: Element,
        animeShowList: ArrayList<Any>
    ) {
        //是首页则跳过
        if (partUrl == "/") return

        val url = Api.MAIN_URL + partUrl
        //页面数据列表
        var listElements: Elements = element.select("div.mod>.area")
        if (listElements.isNullOrEmpty()) return
        for (i in listElements.indices) {
            //视频数据列表
            animeShowList.addAll(CustomParseHtmlUtil.parseLVideo(listElements[i], url))
            //TODO 未完成 图片数据列表
            animeShowList.addAll(CustomParseHtmlUtil.parseLPic(listElements[i], url))
            //TODO 未完成 小说数据列表
//            animeShowList.addAll(ParseHtmlUtil.parseLFiction(listElements[i], url))
        }
    }

    /**
     * 首页tab数据
     */
    private fun updateHomeDate(
        animeShowList: ArrayList<Any>
    ) {

        val url = Api.MAIN_URL

        //初始化首页地址
        println("初始地址：${Api.MAIN_URL}")
        CustomUtil().initApi(Api.MAIN_URL)
        if (url != Api.MAIN_URL) {
            println("更新地址：${Api.MAIN_URL}")
            animeShowList.add(Header1Bean("", "更新成功"))
            animeShowList.add(Header1Bean("", "正在重启app"))
            //重启
            com.skyd.imomoe.util.Util.restartApp()
        } else {
            animeShowList.add(Header1Bean("", "请使用其他选项卡↑"))
            animeShowList.add(Header1Bean("", "无法使用则下拉刷新"))
        }
    }
}