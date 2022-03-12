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
import java.lang.ref.SoftReference
import kotlin.coroutines.resume


class CustomPlayModel : IPlayModel {

    private var mActivity: SoftReference<Activity>? = null


    private fun getVideoUrl(iframeSrc: String, callback: GettingCallback) {
        val activity = mActivity?.get()
        if (activity == null || activity.isDestroyed) throw Exception("activity不存在或状态错误")
        activity.runOnUiThread {
            GettingUtil.instance.activity(activity)
                .url(iframeSrc).start(object : GettingCallback {
                    override fun onGettingSuccess(webView: View?, html: String) {
                        callback.onGettingSuccess(webView, html)
                    }

                    override fun onGettingError(webView: View?, url: String?, errorCode: Int) {
                        callback.onGettingError(webView, url, errorCode)
                    }

                })
        }
    }

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
            "视频损坏或不支持解析！".showToast(Toast.LENGTH_LONG)
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
                                println("解析结果：${videoUrl}")
                                var newUrl = parseM3u8Url(videoM3u8Url)
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

    private fun parseM3u8Url(videoUrl: String): String {
        var newurl = "$videoUrl?skipl=1";
        if (videoUrl.indexOf("?") > 0) {
            newurl = "$videoUrl&skipl=1"
        }
//        var myString = readTxt(newurl)
//        val parser = MasterPlaylistParser()
//        val playlist = parser.readPlaylist(myString)
        return newurl
    }

//    protected fun readTxt(urlStr: String): String? {
//        try {
//            //从params中获取传过来的URL
//            val url = URL(urlStr)
//            Log.e("TAG", urlStr)
//            //使用URLconnection的子类HttpURLconnection来请求连接更好
//            val conn = url.openConnection() as HttpURLConnection
//            conn.doOutput = true //设置要读取文件
//            conn.connectTimeout = 10000 //设置连接的最长时间
//            val `is` = conn.inputStream //获取连接的输入流
//            val baos = ByteArrayOutputStream() //创建一个高速的输出流来读取输入流
//            //对数据的读取（边读边取）
//            var len = 0
//            val buf = ByteArray(1024)
//            while (`is`.read(buf).also { len = it } != -1) {
//                baos.write(buf, 0, len)
//            }
//            //获得的输出流变成String类型的字符串，用于最后的返回
//            val result = String(baos.toByteArray()) //设置编码格式
//            //返回的是获取的一大串文本资源源码
//            Log.e("TAG", "-------------->\t\n$result")
//            return result
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return null
//    }

    override suspend fun playAnotherEpisode(partUrl: String): AnimeEpisodeDataBean {
        val animeEpisodeDataBean = AnimeEpisodeDataBean("", "")
        val url = Api.MAIN_URL + partUrl
        val document = JsoupUtil.getDocument(url)
        getVideoUrl(partUrl, document, animeEpisodeDataBean)
        return animeEpisodeDataBean;
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