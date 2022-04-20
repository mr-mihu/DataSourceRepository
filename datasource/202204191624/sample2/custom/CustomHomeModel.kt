package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IHomeModel
import org.jsoup.select.Elements

class CustomHomeModel : IHomeModel {
    override suspend fun getAllTabData(): ArrayList<TabBean> {
        return ArrayList<TabBean>().apply {
            val document = JsoupUtil.getDocument(Api.MAIN_URL)
            val menu: Elements = document.getElementsByClass("menu")
            val dmx_l: Elements = menu.select("[class=dmx l]").select("li")
            for (i in dmx_l.indices) {
                val url = dmx_l[i].select("a").attr("href")
                add(TabBean(url, Api.MAIN_URL + url, dmx_l[i].text()))
            }
            val dme_r: Elements = menu.select("[class=dme r]").select("li")
            for (i in dme_r.indices) {
                val url = dme_r[i].select("a").attr("href")
                add(TabBean(url, Api.MAIN_URL + url, dme_r[i].text()))
            }
        }
    }
}