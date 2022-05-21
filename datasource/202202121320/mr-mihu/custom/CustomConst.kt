package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.BuildConfig
import com.skyd.imomoe.model.interfaces.IConst

class CustomConst : IConst {

    override val actionUrl = ActionUrl()

    class ActionUrl : IConst.IActionUrl {
        override fun ANIME_RANK(): String = "/vodtype/"
        override fun ANIME_PLAY(): String = "/vodplay/"
        override fun ANIME_DETAIL(): String = "/voddetail/"
        override fun ANIME_SEARCH(): String = "/vodsearch/"
    }

    override fun versionName(): String = "0.0.2"

    override fun versionCode(): Int = 2

    override fun MAIN_URL(): String {
        var site = BuildConfig.URL_HENNIU_SITE
        site = site.replace("24", "")
        return site
    }

    override fun about(): String {
        return "数据来源：${MAIN_URL()}"
    }
}
