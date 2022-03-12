package com.skyd.imomoe.model.impls.custom

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.skyd.imomoe.App
import com.skyd.imomoe.BuildConfig
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.Util.getSubString
import com.skyd.imomoe.util.Util.toEncodedUrl
import com.skyd.imomoe.util.html.SnifferVideo
import okio.ByteString.Companion.decodeBase64
import okio.internal.commonToUtf8String
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.UnsupportedEncodingException
import java.net.URL
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.Base64.getDecoder
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.random.Random


object CustomParseHtmlUtil {

    /**
     * tab标签页视频数据列表
     */
    fun parseLVideo(element: Element, imageReferer: String): List<AnimeCover3Bean> {
        val animeCover3List: MutableList<AnimeCover3Bean> = ArrayList()
        var nodes = element.children()
        for (node in nodes){
            if(!(node.classNames().contains("col5"))) continue
            var results: Elements = node.select("dl dt a")
            for (i in results.indices) {
                //地址
                val url = results[i].attr("href")
                if(!url.startsWith("/")){//非站内链接
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
            if(!url.startsWith("/")){//非站内链接
                continue
            }
            //标题
            var title = results[i].select("h2")[0].ownText()
            //更新时间
            var time = results[i].select("span")[0].ownText()
            var area: AnimeAreaBean = AnimeAreaBean("","","")
            var episodeClickable: AnimeEpisodeDataBean = AnimeEpisodeDataBean("",time,"")
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
        if(elements.isNullOrEmpty()){
            //首页时没有分页标签
            return null
        }
        //下一页的a标签
        var aTabs = elements[0].select("a")
        var pgHo:Element = aTabs[0];//首页
        var pgUp:Element = aTabs[1];//上一页
        var pgDn:Element = aTabs[aTabs.size-2];//下一页
        var pgEn:Element = aTabs[aTabs.size-1];//尾页

        //没有下一页了
        if(pgDn.attr("href")==pgEn.attr("href")){
            return null
        }

        //组装下一页数据
        val title = pgDn.text()
        val url = pgDn.attr("href")
        return PageNumberBean(url, Api.MAIN_URL + url, title)
    }

    fun parseMovurls(
        element: Element,
        selected: AnimeEpisodeDataBean? = null
    ): List<AnimeEpisodeDataBean> {
        val animeEpisodeList: MutableList<AnimeEpisodeDataBean> = ArrayList()

        val elements: Elements = element.select("div.detail-content ul li")
        val urlList : HashSet<String> = HashSet()

        for (k in elements.indices) {
            var aTabs = elements[k].select("a")
            var href = aTabs.attr("href")
            //跳过非播放链接 //当前链接已存在则跳过
            if(!href.startsWith("/")||!urlList.add(href)){
                continue
            }
            //添加到集合
            animeEpisodeList.add(
                AnimeEpisodeDataBean(
                    aTabs.attr("href"),
                    aTabs.attr("title")
                )
            )
        }
        return animeEpisodeList
    }

    /**
     * 首页图片数据列表
     */
    fun parseImg(
        element: Element,
        imageReferer: String
    ): List<AnimeCover1Bean> {
        val animeShowList: MutableList<AnimeCover1Bean> = ArrayList()
        val elements: Elements = element.select("ul").select("li")
        for (i in elements.indices) {
            val aTab = elements[i].select("a")
            val url = aTab.attr("href")
            val title = aTab.attr("title")
            var cover = aTab.select("img").attr("src")
            cover = getCoverUrl(cover,imageReferer)

            var episode = ""
            if (elements[i].select("p").size > 1) {
                episode = elements[i].select("p")[1].select("a").text()
            }
            animeShowList.add(
                AnimeCover1Bean(
                    actionUrl = url, url = Api.MAIN_URL + url,
                    title = title, cover = ImageBean("", cover, imageReferer),
                    episode = episode
                )
            )
        }
        return animeShowList
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

    fun getPlayerRowsData(url:String,element: Element, animeDetailList: MutableList<Any>){
        //获取详情模块的信息列表
        var detailSource = element.select("div.detail-source")
        for (player in detailSource){
            var playerTabs = player.select("ul.detail-tab li a")
            for (i in playerTabs.indices){
                //播放器标题
                animeDetailList.add(Header1Bean("", playerTabs[i].ownText()))

                //线路集
                animeDetailList.add(
                    HorizontalRecyclerView1Bean("", parseMovurls(player))
                )
            }
        }

        //验证播放地址是否正确
        verifyPlayerUrl(url,animeDetailList)
        return
    }

    /**
     * 验证播放地址
     */
    private fun verifyPlayerUrl(url: String,animeDetailList: MutableList<Any>) {
        var urlId:String = getVideoIdByUrl(url)
        for (d in animeDetailList){
            //只处理集类型
            if (d is HorizontalRecyclerView1Bean){
                for (b in d.episodeList){
                    if(b.actionUrl.isNullOrBlank()){
                        continue
                    }
                    var id:String = getVideoIdByUrl(b.actionUrl)
                    if(urlId != id){
                       var newVideoUrl = getUrlVideoPlayerByDetailUrl(urlId,b.actionUrl)
                        b.actionUrl = newVideoUrl
                        println("更换集地址：${b.actionUrl}")
                    }
                }
            }
        }
    }

    private fun getUrlVideoPlayerByDetailUrl(id: String, videoUrl: String): String {
        //  /vodplay/54149-1-1.html

        //54149-1-1.html
        var split = videoUrl.split("/")
        var endUrl = split[split.size - 1]
        //-1-1.html
        var url后缀 = endUrl.substring(endUrl.indexOf("-"), endUrl.length)

        var startIndexOf = videoUrl.lastIndexOf("/")+1
        var endIndexOf = videoUrl.indexOf(url后缀)

        var startString = videoUrl.substring(0, startIndexOf)
        var endString = videoUrl.substring(endIndexOf, videoUrl.length)

        return startString+id+endString
    }

    private fun getVideoIdByUrl(url: String): String {
        //  http://a.b.c/voddetail/54770.html
        //  /vodplay/54149-1-1.html
        var url前缀 = url.substring(0, url.indexOf(".html"))
        // /vodplay/54149-1-1
        var split = url前缀.split("/")
        // 54149-1-1
        var idSection = split.get(split.size - 1)
        // 54149
        return idSection.split("-")[0]
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
        if(!aTabs.isNullOrEmpty()){
            typeUrl = Api.MAIN_URL+aTabs[1].attr("href")
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
            episodesList.add(AnimeEpisodeDataBean(url,title,href))
        }
    }

    /**
     * 解析集信息，通过scipt中的json信息
     * @return Triple，不可为null
     * String： 加密类型（0未加密,1url地址转换,2特殊base64）
     * String： 视频url（https://a.b.c/20211125/8Q6Tps4V/index.m3u8）
     * String： 播放器（xkm3u8）
     */
    fun getAnalysisEpisodes(partUrl: String, videoJsonStr: String): Triple<Int,String,String>  {
        var videoJson:JSONObject = getVideoJson(videoJsonStr)
        var videoStr = videoJson.getString("url")
        var playerName = videoJson.getString("from")
        var encrypt = videoJson.getString("encrypt").toInt()//加密类型：0未加密,1url地址转换,2特殊base64
        if (encrypt == 1) {
            videoStr = videoStr.toEncodedUrl()
        } else if (encrypt == 2) {
            //videoUrl = getEncodedUrl(base64decode(videoUrl));
        }

        //m3u8格式直接返回，否则继续解析地址
        if(videoStr.endsWith(".m3u8")){
            return Triple(encrypt,videoStr,playerName)
        }else if(videoStr.contains(".ddyunbo.com/share/")){
//            videoStr = parseVideoDdyunboTom3u8(videoStr)
            encrypt = 9
            println("不支持的类型：${videoStr}")
        }

        return Triple(encrypt,videoStr,playerName)
    }

    /**
     * 解析视频地址返回m3u8
     */
    @Deprecated("未实现")
    private fun parseVideoDdyunboTom3u8(videoStr: String): String {

        var document:Document = JsoupUtil.getDocumentSynchronously(videoStr)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            var a= "U2FsdGVkX185ocAIpeMURx4O5IQkI8Uuu0683KQnsV368Ja00HqxWDUwX1liGccyR/pTVPxPABU3QWaAAmbYYomEhLCXKXYuXpGzn5wavTxk2Z+d290BxGeHYD7sK1TDqksSbt18l7CjpbQwkNANlxsMhLDa28ZK2qJ7f9KXphK8gs3+aPNqMsXA7cvXv59v6wH+vAUbFH1puyrYAkD+YLuIn3qqwj4eGmiHA0X/N5KYon0Fijn+SJgN67DEBbO+8XaXNYuG6syMHNFDsXxM40CAvbUoGoEh6W2/Lyq/XgghSIAWo/dnqg0AxkPqJ0AxwqoYSiaKKKodfWC42qqSkruFVnYz0PfqCXX8gcF1cmVYKRtwHIPCZBo/6QyVKbF0Iq5sCG/9hlatt7tTp/uVh18o3sXDDNje04WYcgAWjoEkKrkCxwufJ7NyUVctUX1BWZQQvTDeheZt2zCpgS1kRnuMxKIHLjXPyO1SvLvlptJGYO8Pt6ClyG/neWeU2RmwcZZpYzmDJTjU2mpjRdX51HAn3viIo6Ho3O64fTHcGfWVg14QoeakGVrloOJWM6Gxe/foENKewhjejrYaIJm69ri4dwyet0pym4UKjMGZB52rxZzrTVN84x4L1Yujp8UMP6w1FBTtStvPRA1u15mACqGIaYrAzcQA7IjORKUPnAy+E22XgCrr1wtIGByTD9YPeYYjG0Fyfpe0sNuQ62emXcUpbkP9ulH0O6H0VkjFsjSp/AbVN+oQJzOlGRMu1Od4nCf3HCf0PmJ76Ec9mCnw4mpklkdbcoyyx+eH30MDt38uqmx8iWTb1ZRw9yj9Oym4m07tabPNX+MJF1aKeH2DHJ9l8FcyWQVvl5jTnameZF2IbD7OyqtVmTSXyigk428dST7MpH29IAE0xFvXZ2F7EMh9hLxMVURkOZJQIkoJfZO7zOySg+0o5Slf23WuohBzIO1eekDUTsI5EiIsEqZCFcl/5Iwsdj6A/Xt34OcM/edo02wodd8uaW8U20qhDS2KhxgWPcvrj3vf/yWA41JUXj9YHVa74v4B/ESrFKWa0vg="
//            var decryptAES = decryptAES(a, "ppvod")
//            print("解析str:")
//            println(decryptAES)
//        }else{
//            return ""
//        }
        return ""
    }

    /**
     * json字符串解析成JsonArray
     */
    private fun getVideoJson(videoListStr: String):JSONObject {
        if(videoListStr.isBlank()){
            return JSONObject()
        }
        var videoListJson = videoListStr.substring((videoListStr.indexOf("=")+1))
        return JSONObject(videoListJson)
    }

    /**
     * 解析地址上的播放集信息
     */
    fun getUrlParam(url:String):List<Int>{
        var sArr:ArrayList<Int> =  ArrayList()
        var ss = url
        var sid = ss.split("-")
        var n = sid.size
        var pid = sid[n-2]
        var vid = sid[n-1].split(".")[0]
        sArr.add(pid.toInt())
        sArr.add(vid.toInt())
        return sArr;
    }

    /**
     * 获取真正的首页地址
     */
    fun getHomeUrl(apiUrl: String = BuildConfig.API_HENNIUYINGSHI_COM): String {
        //http://api.hnmacapi.xyz/news/data.php
        var homeStr = JsoupUtil.getDocumentSynchronously(apiUrl)
        var linkListText: String = homeStr.text()
        var linkListStr = linkListText.substring(linkListText.indexOf("["), linkListText.indexOf("];") + 1)
        var linkList = JSONArray(linkListStr)
        return linkList.getString(Random.nextInt(linkList.length()-1))
    }

    /**
     * 解析列表页面分类标题
     */
    fun parseLTitle(element: Element, url: String): Header1Bean {
        //标题
        val titleATab: Element = element.select("div.mod-title h3 a")[0]
        return Header1Bean(url, titleATab.ownText())
    }

    /**
     * 相关推荐
     */
    fun getPlayerMoreRowsData(url: String, element: Element, playBeanDataList: ArrayList<Any>) {
        //页面数据列表
        var listElements: Elements = element.select("div.mod .area")
        if (listElements.isNullOrEmpty()) return

        //标题
        playBeanDataList.add(Header1Bean("", "相关推荐"))
        //推荐内容
        for (i in listElements.indices) {
            //视频数据列表
            playBeanDataList.addAll(parseLMore(listElements[i], url))
        }
    }

    /**
     * 获取视频播放页面相关推荐列表
     */
    private fun parseLMore(element: Element, imageReferer: String): Collection<Any> {
        val animeShowList: MutableList<AnimeCover1Bean> = ArrayList()
        val results: Elements = element.select("div.col5 dl dt a")
        for (i in results.indices) {
            //地址
            val url = results[i].attr("href")
            if(!url.startsWith("/")){//非站内链接
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
            animeShowList.add(
                AnimeCover1Bean(
                    actionUrl = url, url = Api.MAIN_URL + url,
                    title = title, cover = ImageBean("", cover, imageReferer),
                    episode = ""
                )
            )
        }
        return animeShowList
    }

    /**
     * 真正的首页地址
     */
    var mainURL = ""
}