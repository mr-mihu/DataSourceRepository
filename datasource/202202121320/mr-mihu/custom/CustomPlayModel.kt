package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.skyd.imomoe.bean.AnimeEpisodeDataBean
import com.skyd.imomoe.bean.AnimeTitleBean
import com.skyd.imomoe.bean.ImageBean
import com.skyd.imomoe.bean.PlayBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.IPlayModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.html.source.GettingCallback
import com.skyd.imomoe.util.html.source.web.GettingUtil
import com.skyd.imomoe.util.showToast
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.SoftReference
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume


class CustomPlayModel : IPlayModel {

    private var mActivity: SoftReference<Activity>? = null


    override suspend fun getPlayData(
        partUrl: String,
        animeEpisodeDataBean: AnimeEpisodeDataBean
    ): Triple<ArrayList<Any>, ArrayList<AnimeEpisodeDataBean>, PlayBean> {
        val playBeanDataList: ArrayList<Any> = ArrayList()
        val episodesList: ArrayList<AnimeEpisodeDataBean> = ArrayList()
        val title = AnimeTitleBean("", "")
        val episode = AnimeEpisodeDataBean("", "")
        val playBean = PlayBean("", title, episode, playBeanDataList)
        val url = Api.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        var element: Element = document.body()

        //获取视频信息
        getVideoUrl(partUrl, element, animeEpisodeDataBean)

        /* 每行模块数据 */
        CustomParseHtmlUtil.getPlayerRowsData(url, element, playBeanDataList)

        //获取相关推荐
        //ParseHtmlUtil.getPlayerMoreRowsData(url,element,playBeanDataList)

        //获取所有集信息
        CustomParseHtmlUtil.getAllEpisodes(url, element, episodesList)

        //封装到当前播放bean
        playBean.title = title
        playBean.episode = episode
        playBean.data = playBeanDataList
        return Triple(playBeanDataList, episodesList, playBean)
    }

    /**
     * 获取视频信息
     */
    private suspend fun getVideoUrl(
        partUrl: String,
        element: Element,
        animeEpisodeDataBean: AnimeEpisodeDataBean
    ) {
        val script: Element = element.select("div.bofang_box script").find {
            it.html().contains("player_aaaa", true)
        } ?: return

        var videoInfoStr = script.html()
        //[加密类型,地址,播放器]
        var videoAny = CustomParseHtmlUtil.getAnalysisEpisodes(partUrl, videoInfoStr)
        println(videoAny)
        var videoUrl = videoAny.second
        var encrypt = videoAny.first

        animeEpisodeDataBean.videoUrl = videoUrl
        animeEpisodeDataBean.title = element.select("h1.h1-title")[0].text()

        //加密视频
        if (encrypt == 9) {
            "请点击右上角使用外部播放器播放！".showToast(Toast.LENGTH_LONG)
//            Util.openVideoByExternalPlayer(Utils.getAppContext(),videoUrl)
            //解析video地址
            var newUrl: String = suspendCancellableCoroutine {
                val activity = mActivity?.get()
                if (activity == null || activity.isDestroyed) throw Exception("activity不存在或状态错误")
                activity.runOnUiThread {
                    GettingUtil.instance.activity(activity).url(videoUrl)
                        .start(object : GettingCallback {
                            override fun onGettingSuccess(webView: View?, html: String) {
                                GettingUtil.instance.release()
                                val iframe = Jsoup.parse(html)
                                var videoM3u8Url =
                                    iframe.body().select("div.dplayer-video-wrap video")
                                        .attr("ppp-src")
                                println("解析视频地址：${videoUrl}")
                                var newUrl = parseM3u8Url(videoM3u8Url, videoUrl)
                                it.resume(newUrl)
                            }

                            override fun onGettingError(
                                webView: View?,
                                url: String?,
                                errorCode: Int
                            ) {
                                GettingUtil.instance.release()
                            }

                        })
                }
            }

            animeEpisodeDataBean.videoUrl = newUrl

        }
        println("视频地址：${animeEpisodeDataBean.videoUrl}")
    }

    private fun parseM3u8Url(videoUrl: String, videoUrlSrc: String): String {
        var newurl = "$videoUrl?skipl=1"
        if (videoUrl.indexOf("?") > 0) {
            newurl = "$videoUrl&skipl=1"
        }

        //目前只适配ddyunbo.com域名的解析
        if (videoUrlSrc.contains("ddyunbo") && newurl.startsWith("/")) {
            var prefix = ""
            if (videoUrlSrc.indexOf("http") != -1) {
                /* https://vip5.ddyunbo.com/share/CZyUH5qWq6jnFRgr */
                //协议：https://
                var http = videoUrlSrc.substring(0, (videoUrlSrc.indexOf("//") + 2))
                //域名：vip5.ddyunbo.com
                var domainName = videoUrlSrc.substringAfter(http).substringBefore("/")
                //拼接出完整域名：https://vip5.ddyunbo.com
                prefix = http + domainName
            }
            newurl = prefix + newurl
            newurl = parseDdyunboUrl(newurl, prefix)

        }
        return newurl
    }

    private fun parseDdyunboUrl(videoUrl: String, prefix: String): String {
        var readTxt = readTxt(videoUrl)
        var lineList = readTxt.split("\n")
        if (lineList.size >= 3) {
            return prefix + lineList[2]
        }
        return videoUrl
    }

    private fun readTxt(url: String): String {
        val inputstreamreader = BufferedReader(InputStreamReader(URL(url).openStream()))
        try {
            return inputstreamreader.readText()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            inputstreamreader.close()
        }
        "读取失败,请检查文件名称及文件是否存在!".showToast(Toast.LENGTH_LONG)
        return ""
    }

    override suspend fun playAnotherEpisode(partUrl: String): AnimeEpisodeDataBean {
        val animeEpisodeDataBean = AnimeEpisodeDataBean("", "")
        val url = Api.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        getVideoUrl(partUrl, document, animeEpisodeDataBean)
        return animeEpisodeDataBean
    }

    override suspend fun getAnimeCoverImageBean(detailPartUrl: String): ImageBean? {
        val url = Api.MAIN_URL + detailPartUrl
        val document = JsoupUtil.getDocument(url)
        var imgTab = document.select("body img")[0]
        return ImageBean(
            "",
            CustomParseHtmlUtil.getCoverUrl(imgTab.attr("src"), url),
            url
        )
    }

    override fun setActivity(activity: Activity) {
        mActivity = SoftReference(activity)
    }

    override fun clearActivity() {
        GettingUtil.instance.releaseAll()
        mActivity = null
    }

    override suspend fun getAnimeDownloadUrl(partUrl: String): String? {
        val url = Api.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        var element: Element = document.body()
        val animeEpisodeDataBean = AnimeEpisodeDataBean("", "")
        getVideoUrl(partUrl, element, animeEpisodeDataBean)
        return animeEpisodeDataBean.videoUrl
    }

}