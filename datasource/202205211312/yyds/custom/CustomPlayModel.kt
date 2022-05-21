package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import com.google.gson.reflect.TypeToken
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IPlayModel
import com.skyd.imomoe.net.RetrofitManager

class CustomPlayModel : IPlayModel {
    override suspend fun getPlayData(
        partUrl: String,
        animeEpisodeDataBean: AnimeEpisodeDataBean
    ): Triple<ArrayList<Any>, ArrayList<AnimeEpisodeDataBean>, PlayBean> {
        val myJsonParser: MyJsonParser<BaseBean> =
            MyJsonParser(typeElementName = "type", targetClass = BaseBean::class.java)
                .addTypeElementValueWithClassType("AnimeCover1", AnimeCover1Bean::class.java)
                .addTypeElementValueWithClassType("Header1", Header1Bean::class.java)
                .addTypeElementValueWithClassType(
                    "HorizontalRecyclerView1",
                    HorizontalRecyclerView1Bean::class.java
                )
                .addTypeElementValueWithClassType("AnimeEpisode1", AnimeEpisode1Bean::class.java)
                .addTypeElementValueWithClassType("AnimeTitleBean", AnimeTitleBean::class.java)
                .addTypeElementValueWithClassType("PlayBean", PlayBean::class.java)
                .addTypeElementValueWithClassType(
                    "AnimeEpisodeDataBean",
                    AnimeEpisodeDataBean::class.java
                )
                .create()

        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getPlayData(CustomConst.MAIN_URL + partUrl)

        val first = myJsonParser.fromJson<ArrayList<Any>>(
            raw.data!!.first.toString(),
            object : TypeToken<ArrayList<BaseBean>>() {}.type
        )

        val second: ArrayList<AnimeEpisodeDataBean> = arrayListOf()
        first.forEach {
            if (it is HorizontalRecyclerView1Bean) {
                second.addAll(it.episodeList)
            }
        }

        val third = myJsonParser.fromJson<PlayBean>(
            raw.data.third.toString(),
            object : TypeToken<BaseBean>() {}.type
        )

        third.detailPartUrl = "/getAnimeDetailData?url=" +
                raw.data.third.asJsonObject.get("detail").asJsonObject.get("url").asString

        animeEpisodeDataBean.title = third.episode.title
        animeEpisodeDataBean.videoUrl = third.episode.videoUrl

        return Triple(first, second, third)
    }

    override suspend fun playAnotherEpisode(partUrl: String): AnimeEpisodeDataBean {
        val animeEpisodeDataBean = AnimeEpisodeDataBean(route = "", title = "")
        getPlayData(partUrl = partUrl, animeEpisodeDataBean = animeEpisodeDataBean)
        return animeEpisodeDataBean
    }

    override suspend fun getAnimeCoverImageBean(partUrl: String): ImageBean {
        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getPlayData(CustomConst.MAIN_URL + partUrl)
        return ImageBean(
            url = raw.data!!.third.asJsonObject.get("cover").asJsonObject.get("url").asString
        )
    }

    override fun setActivity(activity: Activity) {
    }

    override fun clearActivity() {
    }

    override suspend fun getAnimeDownloadUrl(partUrl: String): String? {
        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getAnimeDownloadUrl(
                CustomConst.MAIN_URL + partUrl.replaceFirst(
                    "/getPlayData",
                    "/getAnimeDownloadUrl"
                )
            )
        return raw.data!!.asJsonObject.get("videoUrl").asString
    }
}