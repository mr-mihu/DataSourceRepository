package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.*
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.util.Util.toEncodedUrl
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL


object CustomParseHtmlUtil {

    /**
     * tab标签页视频数据列表
     */
    fun parseLVideo(element: Element, imageReferer: String): List<AnimeCover3Bean> {
        val animeCover3List: MutableList<AnimeCover3Bean> = ArrayList()
        var nodes = element.children()
        for (node in nodes) {
            if (!(node.classNames().contains("col5"))) continue
            var results: Elements = node.select("dl dt a")
            for (i in results.indices) {
                //地址
                val url = results[i].attr("href")
                if (!url.startsWith("/")||!url.contains(CustomConst().actionUrl.ANIME_DETAIL(),true)) {//非站内链接
                    continue
                }
                //标题
                val title = results[i].attr("title")
                //视频图片
                var cover = results[i].select(".nature").attr("data-original")
                cover = getCoverUrl(
                    cover,
                    imageReferer
                )
                val animeType: MutableList<AnimeTypeBean> = ArrayList()
                animeCover3List.add(
                    AnimeCover3Bean(
                        actionUrl = url,
                        url = Api.MAIN_URL + url,
                        title = title,
                        cover = ImageBean("", cover, imageReferer),
                        episode = "",
                        animeType = animeType,
                        describe = title
                    )
                )
            }
        }
        return animeCover3List
    }

    /**
     * tab标签页图片数据列表
     */
    fun parseLPic(element: Element, imageReferer: String): List<AnimeCover5Bean> {
        val animeCover5List: MutableList<AnimeCover5Bean> = ArrayList()
        val results: Elements = element.select("#colList>ul>li>a")
        for (i in results.indices) {
            //地址
            var url = results[i].attr("href")
            if (!url.startsWith("/")) {//非站内链接
                continue
            }
            //标题
            var title = results[i].select("h2")[0].ownText()
            //更新时间
            var time = results[i].select("span")[0].ownText()
            var area: AnimeAreaBean = AnimeAreaBean("", "", "")
            var episodeClickable: AnimeEpisodeDataBean = AnimeEpisodeDataBean("", time, "")
            animeCover5List.add(
                AnimeCover5Bean(
                    actionUrl = url,
                    url = Api.MAIN_URL + url,
                    title = title,
                    area,
                    "",
                    episodeClickable
                )
            )
        }
        return animeCover5List
    }

    /**
     * 只获取下一页的地址，没有下一页则返回null
     */
    fun parseNextPages(elements: Elements): PageNumberBean? {
        if (elements.isNullOrEmpty()) {
            //首页时没有分页标签
            return null
        }
        //下一页的a标签
        var aTabs = elements[0].select("a")
        var pgHo: Element = aTabs[0]//首页
        var pgUp: Element = aTabs[1]//上一页
        var pgDn: Element = aTabs[aTabs.size - 2]//下一页
        var pgEn: Element = aTabs[aTabs.size - 1]//尾页

        //没有下一页了
        if (pgDn.attr("href") == pgEn.attr("href")) {
            return null
        }

        //组装下一页数据
        val title = pgDn.text()
        val url = pgDn.attr("href")
        return PageNumberBean(url, Api.MAIN_URL + url, title)
    }

    private fun getUrlVideoPlayerByDetailUrl(id: String, videoUrl: String): String {
        //  /vodplay/54149-1-1.html

        //54149-1-1.html
        var split = videoUrl.split("/")
        var endUrl = split[split.size - 1]
        //-1-1.html
        var urlPrefix = endUrl.substring(endUrl.indexOf("-"), endUrl.length)

        var startIndexOf = videoUrl.lastIndexOf("/") + 1
        var endIndexOf = videoUrl.indexOf(urlPrefix)

        var startString = videoUrl.substring(0, startIndexOf)
        var endString = videoUrl.substring(endIndexOf, videoUrl.length)

        return startString + id + endString
    }

    private fun getVideoIdByUrl(url: String): String {
        //  http://a.b.c/voddetail/54770.html
        //  /vodplay/54149-1-1.html
        var urlPrefix = url.substring(0, url.indexOf(".html"))
        // /vodplay/54149-1-1
        var split = urlPrefix.split("/")
        // 54149-1-1
        var idSection = split.get(split.size - 1)
        if(idSection.indexOf("-")!=-1){
            idSection = idSection.split("-")[0]
        }
        // 54149
        return idSection
    }

    /**
     * 添加详情信息
     */
    fun addInfoBean(url: String, element: Element, animeDetailList: MutableList<Any>) {
        //标题
        var title = ""
        var alias = ""
        var info = ""
        var area = ""
        var year = ""
        var index = ""
        val typeList: MutableList<AnimeTypeBean> = ArrayList()
        val tagList: MutableList<AnimeTypeBean> = ArrayList()
        //分类链接
        var typeUrl = ""
        var detailElement = element.select("div.detail.clearfix")

        //从导航栏获取分类链接
        val aTabs = element.select("div.breadcrumbs a")
        if (!aTabs.isNullOrEmpty()) {
            typeUrl = Api.MAIN_URL + aTabs[1].attr("href")
            //typeList.add(AnimeTypeBean("",typeUrl,aTabs[1].text()))
        }

        //封面图片
        var img = detailElement.select("div.detail-poster img")
        var imgSrc = img.attr("src")
        val cover = ImageBean("", imgSrc, url)
        //标题
        title = img.attr("alt")
        //索引取第一个字符
        //index = title.substring(0,1)

        //详情相关内容
//        var ul = detailElement.select("div.detail-info .detail-actor")
//        for (li in ul){
//            var label = li.select("label").text()
//            var name = li.ownText()
//            //番剧头部信息
//            if(label.startsWith("更新")){
//                year = name
//            }else if(label.startsWith("来源")){
//                area = name
//            }else if(label.startsWith("画质")){
//                tagList.add(AnimeTypeBean("","",name))
//            }
//        }

        //视频其他信息div
        animeDetailList.add(
            0,
            AnimeInfo1Bean(
                "",
                title,
                cover,
                alias,
                area,
                year,
                index,
                typeList,
                tagList,
                info
            )
        )
    }

    /**
     * 获取所有集信息
     */
    fun getAllEpisodes(
        url: String,
        element: Element,
        episodesList: ArrayList<AnimeEpisodeDataBean>
    ) {
        var aTabs = element.select("div.playlist li a")
        for (atab in aTabs) {
            var title = atab.text()
            var href = atab.attr("href")
            episodesList.add(AnimeEpisodeDataBean(url, title, href))
        }
    }

    /**
     * 解析集信息，通过scipt中的json信息
     * @return Triple，不可为null
     * String： 加密类型（0未加密,1url地址转换,2特殊base64）
     * String： 视频url（https://a.b.c/20211125/8Q6Tps4V/index.m3u8）
     * String： 播放器（xkm3u8）
     */
    fun getAnalysisEpisodes(partUrl: String, videoJsonStr: String): Triple<Int, String, String> {
        var videoJson: JSONObject = getVideoJson(videoJsonStr)
        var videoStr = videoJson.getString("url")
        var playerName = videoJson.getString("from")
        var encrypt = videoJson.getString("encrypt").toInt()//加密类型：0未加密,1url地址转换,2特殊base64
        if (encrypt == 1) {
            videoStr = videoStr.toEncodedUrl()
        } else if (encrypt == 2) {
            //videoUrl = getEncodedUrl(base64decode(videoUrl));
        }

        //m3u8格式直接返回，否则继续解析地址
        if (videoStr.endsWith(".m3u8")) {
            return Triple(encrypt, videoStr, playerName)
        } else if (videoStr.contains(".ddyunbo.com/share/")) {
//            videoStr = parseVideoDdyunboTom3u8(videoStr)
            encrypt = 9
            println("不支持的类型：${videoStr}")
        }

        return Triple(encrypt, videoStr, playerName)
    }

    /**
     * json字符串解析成JsonArray
     */
    private fun getVideoJson(videoListStr: String): JSONObject {
        if (videoListStr.isBlank()) {
            return JSONObject()
        }
        var videoListJson = videoListStr.substring((videoListStr.indexOf("=") + 1))
        return JSONObject(videoListJson)
    }

    /**
     * 解析地址上的播放集信息
     */
    fun getUrlParam(url: String): List<Int> {
        var sArr: ArrayList<Int> = ArrayList()
        var ss = url
        var sid = ss.split("-")
        var n = sid.size
        var pid = sid[n - 2]
        var vid = sid[n - 1].split(".")[0]
        sArr.add(pid.toInt())
        sArr.add(vid.toInt())
        return sArr
    }

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                Api.MAIN_URL + cover
            }
            else -> cover
        }
    }

    fun parseMovurls(
        element: Element,
        selected: AnimeEpisodeDataBean? = null
    ): List<AnimeEpisodeDataBean> {
        val animeEpisodeList: MutableList<AnimeEpisodeDataBean> = ArrayList()

        val elements: Elements = element.select("div.detail-content ul li")
        val urlMap:HashMap<String,String> = HashMap()

        for (episodeElements in elements.indices) {
            var aTabs = elements[episodeElements].select("a")
            var href = aTabs.attr("href")
            var title = aTabs.attr("title")
            //跳过广告
            if(!href.contains(CustomConst().actionUrl.ANIME_PLAY(),true)){
                continue
            }
            //跳过已存在的线路地址
            if(urlMap.containsKey(href)) {
                continue
            }
            //播放线路存起来
            urlMap.put(href,title)
            animeEpisodeList.add(AnimeEpisodeDataBean(href,title))
        }

        //清理不用了
        urlMap.clear()
        //排序线路名称
//        var sortedBy =
//            animeEpisodeList.sortedBy { "s(\\d+)".toRegex().matchEntire(it.title)?.groups?.get(1)?.value?.toInt() }

        return animeEpisodeList
    }

    /**
     * 验证播放地址
     */
    private fun verifyPlayerUrl(url: String, animeDetailList: MutableList<Any>) {
        var urlId: String = getVideoIdByUrl(url)
        for (anyObj in animeDetailList) {
            //只处理集类型
            if (!(anyObj is HorizontalRecyclerView1Bean)) {
                continue
            }

            for (view1Bean in anyObj.episodeList) {
                if (view1Bean.actionUrl.isNullOrBlank()) {
                    continue
                }
                var id: String = getVideoIdByUrl(view1Bean.actionUrl)
                if (urlId != id) {
                    var newVideoUrl = getUrlVideoPlayerByDetailUrl(urlId, view1Bean.actionUrl)
                    view1Bean.actionUrl = newVideoUrl
                    println("更换集地址：${view1Bean.actionUrl}")
                }
            }
        }
    }

    fun getPlayerRowsData(url: String, element: Element, animeDetailList: MutableList<Any>) {
        //获取详情模块的信息列表
        var detailSource = element.select("div.detail-source")
        for (player in detailSource) {
            var playerTabs = player.select("ul.detail-tab li a")
            for (i in playerTabs.indices) {
                //播放器标题
                animeDetailList.add(Header1Bean("", playerTabs[i].ownText()))

                //线路集
                animeDetailList.add(
                    HorizontalRecyclerView1Bean("", parseMovurls(player))
                )
            }
        }

        //验证播放地址是否正确
        verifyPlayerUrl(url, animeDetailList)
        return
    }

}