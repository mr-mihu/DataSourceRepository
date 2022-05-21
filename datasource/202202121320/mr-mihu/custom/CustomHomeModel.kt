package com.skyd.imomoe.model.impls.custom

import android.widget.Toast
import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.IHomeModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.showToast
import org.jsoup.select.Elements

class CustomHomeModel : IHomeModel {
    override suspend fun getAllTabData(): ArrayList<TabBean> {
        "请稍后正在加载数据...".showToast(Toast.LENGTH_LONG)

        var apiUrl = Api.MAIN_URL;

        var list = ArrayList<TabBean>()
        val document = JsoupUtil.getDocument(apiUrl)
        val mainTabs: Elements = document.select("div#menu .first a")
        for (i in mainTabs.indices) {
            val url = mainTabs[i].attr("href")
            if (!url.startsWith("/")) { //非站内链接
                continue
            }
            list.add(TabBean(url, apiUrl + url, mainTabs[i].text()))
        }

        return list
    }
}