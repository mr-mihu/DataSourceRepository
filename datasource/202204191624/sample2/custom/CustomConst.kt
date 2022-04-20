package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.BuildConfig
import com.skyd.imomoe.model.interfaces.IConst

class CustomConst : IConst {
    companion object {
        val ANIME_RANK: String = "/ranklist/"
        val ANIME_PLAY: String = "/vp/"
        val ANIME_DETAIL: String = "/showp/"
        val ANIME_SEARCH: String = "/s_all"
        val ANIME_LINK: String = "/link/"
        val MAIN_URL: String by lazy { CustomConst().MAIN_URL }
    }

    override val MAIN_URL: String
        get() = BuildConfig.CUSTOM_DATA_MAIN_URL

    override fun versionName(): String = "1.1.0"

    override fun versionCode(): Int = 5

    override fun about(): String {
        return "数据来源：${MAIN_URL}"
    }
}
